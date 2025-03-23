package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.config.ApplicationProperties;
import com.patikadev.definex.advancedtaskmanager.constant.FileConstants;
import com.patikadev.definex.advancedtaskmanager.exception.FileStorageException;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.mapper.AttachmentMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.attachment.CreateAttachmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.attachment.AttachmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Attachment;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.repository.AttachmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.impl.AttachmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private AttachmentMapper attachmentMapper;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ApplicationProperties.File fileProperties;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @TempDir
    static Path tempDir;

    private Attachment attachment;
    private Task task;
    private User user;
    private CreateAttachmentRequest createAttachmentRequest;
    private AttachmentResponse attachmentResponse;
    private List<Attachment> attachmentList;
    private List<AttachmentResponse> attachmentResponseList;
    private MultipartFile multipartFile;
    private final Long attachmentId = 1L;
    private final UUID taskId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String uploadDir = tempDir.toString();
    private final String fileName = "test-file.pdf";
    private final String contentType = "application/pdf";
    private final String filePath = "task-attachments/" + taskId + "/123456_test-file.pdf";

    @BeforeEach
    void setUp() {
        task = createTask();
        user = createUser();
        attachment = createAttachment();
        createAttachmentRequest = createCreateAttachmentRequest();
        attachmentResponse = createAttachmentResponse();
        attachmentList = createAttachmentList();
        attachmentResponseList = createAttachmentResponseList();
        multipartFile = createMultipartFile();
    }

    @Test
    @DisplayName("Upload File - Success")
    void uploadFile_Success() throws IOException {
        Files.createDirectories(tempDir.resolve(FileConstants.TASK_ATTACHMENTS_DIR).resolve(taskId.toString()));

        when(applicationProperties.getFile()).thenReturn(fileProperties);
        when(fileProperties.getUploadDir()).thenReturn(uploadDir);
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(attachmentMapper.toEntity(anyString(), any(Task.class), any(User.class), anyString(), anyLong(), anyString())).thenReturn(attachment);
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
        when(attachmentMapper.toResponse(any(Attachment.class))).thenReturn(attachmentResponse);

        AttachmentResponse result = attachmentService.uploadFile(createAttachmentRequest, multipartFile);

        assertNotNull(result);
        assertEquals(attachmentResponse, result);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUserId();
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(attachmentMapper).toEntity(anyString(), eq(task), eq(user), anyString(), eq(multipartFile.getSize()), eq(multipartFile.getContentType()));
        verify(attachmentRepository).save(attachment);
        verify(attachmentMapper).toResponse(attachment);
    }

    @Test
    @DisplayName("Upload File - Empty File")
    void uploadFile_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(FileStorageException.class, () -> attachmentService.uploadFile(createAttachmentRequest, emptyFile));
        verify(taskRepository, never()).findByIdAndIsActiveTrue(any());
        verify(authService, never()).getCurrentUserId();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(attachmentMapper, never()).toEntity(anyString(), any(), any(), anyString(), anyLong(), anyString());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Upload File - Invalid File Type")
    void uploadFile_InvalidFileType() {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.exe", "application/x-msdownload", "test content".getBytes());

        assertThrows(FileStorageException.class, () -> attachmentService.uploadFile(createAttachmentRequest, invalidFile));
        verify(taskRepository, never()).findByIdAndIsActiveTrue(any());
        verify(authService, never()).getCurrentUserId();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(attachmentMapper, never()).toEntity(anyString(), any(), any(), anyString(), anyLong(), anyString());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Upload File - Task Not Found")
    void uploadFile_TaskNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.uploadFile(createAttachmentRequest, multipartFile));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService, never()).getCurrentUserId();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(attachmentMapper, never()).toEntity(anyString(), any(), any(), anyString(), anyLong(), anyString());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Upload File - User Not Found")
    void uploadFile_UserNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.uploadFile(createAttachmentRequest, multipartFile));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUserId();
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(attachmentMapper, never()).toEntity(anyString(), any(), any(), anyString(), anyLong(), anyString());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Download File - Success")
    void downloadFile_Success() throws IOException {
        when(applicationProperties.getFile()).thenReturn(fileProperties);
        when(fileProperties.getUploadDir()).thenReturn(uploadDir);

        Path directory = tempDir.resolve(FileConstants.TASK_ATTACHMENTS_DIR).resolve(taskId.toString());
        Files.createDirectories(directory);

        Path filePath = directory.resolve("123456_" + fileName);
        Files.write(filePath, "test content".getBytes());

        attachment.setFilePath(tempDir.relativize(filePath).toString().replace("\\", "/"));

        when(attachmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(attachment));

        Resource resource = attachmentService.downloadFile(attachmentId);

        assertNotNull(resource);
        assertTrue(resource.exists());
        verify(attachmentRepository).findByIdAndIsActiveTrue(attachmentId);
    }

    @Test
    @DisplayName("Download File - Attachment Not Found")
    void downloadFile_AttachmentNotFound() {
        when(attachmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.downloadFile(attachmentId));
        verify(attachmentRepository).findByIdAndIsActiveTrue(attachmentId);
    }

    @Test
    @DisplayName("Download File - File Not Found")
    void downloadFile_FileNotFound() {
        when(applicationProperties.getFile()).thenReturn(fileProperties);
        when(fileProperties.getUploadDir()).thenReturn(uploadDir);
        when(attachmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(attachment));

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.downloadFile(attachmentId));
        verify(attachmentRepository).findByIdAndIsActiveTrue(attachmentId);
    }

    @Test
    @DisplayName("Get Attachment By Id - Success")
    void getAttachmentById_Success() {
        when(attachmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(attachment));
        when(attachmentMapper.toResponse(any(Attachment.class))).thenReturn(attachmentResponse);

        AttachmentResponse result = attachmentService.getAttachmentById(attachmentId);

        assertNotNull(result);
        assertEquals(attachmentResponse, result);
        verify(attachmentRepository).findByIdAndIsActiveTrue(attachmentId);
        verify(attachmentMapper).toResponse(attachment);
    }

    @Test
    @DisplayName("Get Attachment By Id - Not Found")
    void getAttachmentById_NotFound() {
        when(attachmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.getAttachmentById(attachmentId));
        verify(attachmentRepository).findByIdAndIsActiveTrue(attachmentId);
        verify(attachmentMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Get Attachments By Task Id - Success")
    void getAttachmentsByTaskId_Success() {
        when(taskRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(attachmentRepository.findAllByTaskIdWithDetails(any(UUID.class))).thenReturn(attachmentList);
        when(attachmentMapper.toResponseList(anyList())).thenReturn(attachmentResponseList);

        List<AttachmentResponse> result = attachmentService.getAttachmentsByTaskId(taskId);

        assertNotNull(result);
        assertEquals(attachmentResponseList, result);
        assertEquals(attachmentResponseList.size(), result.size());
        verify(taskRepository).existsByIdAndIsActiveTrue(taskId);
        verify(attachmentRepository).findAllByTaskIdWithDetails(taskId);
        verify(attachmentMapper).toResponseList(attachmentList);
    }

    @Test
    @DisplayName("Get Attachments By Task Id - Task Not Found")
    void getAttachmentsByTaskId_TaskNotFound() {
        when(taskRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.getAttachmentsByTaskId(taskId));
        verify(taskRepository).existsByIdAndIsActiveTrue(taskId);
        verify(attachmentRepository, never()).findAllByTaskIdWithDetails(any());
        verify(attachmentMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Get Attachments By Uploaded User Id - Success")
    void getAttachmentsByUploadedUserId_Success() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(attachmentRepository.findAllByUploadedByUserIdAndIsActiveTrue(any(UUID.class))).thenReturn(attachmentList);
        when(attachmentMapper.toResponseList(anyList())).thenReturn(attachmentResponseList);

        List<AttachmentResponse> result = attachmentService.getAttachmentsByUploadedUserId(userId);

        assertNotNull(result);
        assertEquals(attachmentResponseList, result);
        assertEquals(attachmentResponseList.size(), result.size());
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(attachmentRepository).findAllByUploadedByUserIdAndIsActiveTrue(userId);
        verify(attachmentMapper).toResponseList(attachmentList);
    }

    @Test
    @DisplayName("Get Attachments By Uploaded User Id - User Not Found")
    void getAttachmentsByUploadedUserId_UserNotFound() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.getAttachmentsByUploadedUserId(userId));
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(attachmentRepository, never()).findAllByUploadedByUserIdAndIsActiveTrue(any());
        verify(attachmentMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Delete Attachment - Success")
    void deleteAttachment_Success() {
        when(attachmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(attachment));
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

        attachmentService.deleteAttachment(attachmentId);

        assertFalse(attachment.getIsActive());
        verify(attachmentRepository).findByIdAndIsActiveTrue(attachmentId);
        verify(attachmentRepository).save(attachment);
    }

    @Test
    @DisplayName("Delete Attachment - Not Found")
    void deleteAttachment_NotFound() {
        when(attachmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.deleteAttachment(attachmentId));
        verify(attachmentRepository).findByIdAndIsActiveTrue(attachmentId);
        verify(attachmentRepository, never()).save(any());
    }

    private Task createTask() {
        Task task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .userStory("Test User Story")
                .acceptanceCriteria("Test Acceptance Criteria")
                .priority(TaskPriority.HIGH)
                .state(TaskState.BACKLOG)
                .project(new Project())
                .build();
        task.setIsActive(true);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private User createUser() {
        User user = User.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .password("Password123!")
                .build();
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private Attachment createAttachment() {
        Attachment attachment = Attachment.builder()
                .id(attachmentId)
                .fileName(fileName)
                .filePath(filePath)
                .fileSize(100L)
                .contentType(contentType)
                .task(task)
                .uploadedByUser(user)
                .build();
        attachment.setIsActive(true);
        attachment.setCreatedAt(LocalDateTime.now());
        attachment.setUpdatedAt(LocalDateTime.now());
        return attachment;
    }

    private CreateAttachmentRequest createCreateAttachmentRequest() {
        CreateAttachmentRequest request = new CreateAttachmentRequest();
        request.setTaskId(taskId);
        return request;
    }

    private AttachmentResponse createAttachmentResponse() {
        AttachmentResponse response = new AttachmentResponse();
        response.setId(attachmentId);
        response.setFileName(fileName);
        response.setFilePath(filePath);
        response.setFileSize(100L);
        response.setContentType(contentType);
        response.setTaskId(taskId);
        response.setTaskTitle("Test Task");
        response.setUploadedByUserId(userId);
        response.setUploadedByUserName("Test User");
        response.setCreatedAt(LocalDateTime.now());
        response.setActive(true);
        return response;
    }

    private List<Attachment> createAttachmentList() {
        return Collections.singletonList(attachment);
    }

    private List<AttachmentResponse> createAttachmentResponseList() {
        return Collections.singletonList(attachmentResponse);
    }

    private MultipartFile createMultipartFile() {
        return new MockMultipartFile("file", fileName, contentType, "test content".getBytes());
    }
} 