package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.config.ApplicationProperties;
import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.constant.FileConstants;
import com.patikadev.definex.advancedtaskmanager.exception.FileStorageException;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.mapper.AttachmentMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.attachment.CreateAttachmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.attachment.AttachmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Attachment;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.repository.AttachmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.AttachmentService;
import com.patikadev.definex.advancedtaskmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final AttachmentMapper attachmentMapper;
    private final ApplicationProperties applicationProperties;

    @Override
    @Transactional
    public AttachmentResponse uploadFile(CreateAttachmentRequest request, MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException(ErrorMessages.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType)) {
            throw new FileStorageException(ErrorMessages.FILE_TYPE_NOT_ALLOWED.formatted(contentType));
        }

        Task task = findTaskById(request.getTaskId());
        User currentUser = findCurrentUser();

        String fileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        String relativePath = Paths.get(FileConstants.TASK_ATTACHMENTS_DIR, task.getId().toString(), uniqueFileName).toString();
        Path targetLocation = getUploadPath().resolve(relativePath);

        try {
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = attachmentMapper.toEntity(
                    fileName,
                    task,
                    currentUser,
                    relativePath,
                    file.getSize(),
                    file.getContentType()
            );

            Attachment savedAttachment = attachmentRepository.save(attachment);
            return attachmentMapper.toResponse(savedAttachment);
        } catch (IOException ex) {
            throw new FileStorageException(ErrorMessages.FILE_STORAGE_ERROR.formatted(fileName), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(Long id) {
        Attachment attachment = findAttachmentById(id);

        try {
            Path filePath = getUploadPath().resolve(attachment.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException(ErrorMessages.FILE_NOT_FOUND.formatted(attachment.getFileName()));
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException(ErrorMessages.FILE_NOT_FOUND.formatted(attachment.getFileName()), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachmentById(Long id) {
        Attachment attachment = findAttachmentById(id);
        return attachmentMapper.toResponse(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByTaskId(UUID taskId) {
        if (!taskRepository.existsByIdAndIsActiveTrue(taskId)) {
            throw new ResourceNotFoundException(ErrorMessages.TASK_NOT_FOUND.formatted(taskId));
        }

        List<Attachment> attachments = attachmentRepository.findAllByTaskIdWithDetails(taskId);
        return attachmentMapper.toResponseList(attachments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByUploadedUserId(UUID userId) {
        if (!userRepository.existsByIdAndIsActiveTrue(userId)) {
            throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND.formatted(userId));
        }

        List<Attachment> attachments = attachmentRepository.findAllByUploadedByUserIdAndIsActiveTrue(userId);
        return attachmentMapper.toResponseList(attachments);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long id) {
        Attachment attachment = findAttachmentById(id);

        attachment.setIsActive(false);
        attachmentRepository.save(attachment);
    }

    private Path getUploadPath() {
        String uploadDir = applicationProperties.getFile().getUploadDir();
        if (uploadDir == null || uploadDir.isEmpty()) {
            uploadDir = FileConstants.BASE_UPLOAD_DIR;
        }
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private boolean isAllowedFileType(String contentType) {
        String[] allowedTypes = combineArrays(FileConstants.ALLOWED_IMAGE_TYPES, FileConstants.ALLOWED_DOCUMENT_TYPES);
        return Arrays.asList(allowedTypes).contains(contentType);
    }

    private String[] combineArrays(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private Task findTaskById(UUID taskId) {
        return taskRepository.findByIdAndIsActiveTrue(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.TASK_NOT_FOUND.formatted(taskId)));
    }

    private User findCurrentUser() {
        UUID currentUserId = authService.getCurrentUserId();
        return userRepository.findByIdAndIsActiveTrue(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.USER_NOT_FOUND.formatted(currentUserId)));
    }

    private Attachment findAttachmentById(Long id) {
        return attachmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.ATTACHMENT_NOT_FOUND.formatted(id)));
    }
} 