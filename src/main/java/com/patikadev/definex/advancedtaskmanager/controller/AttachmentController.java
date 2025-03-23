package com.patikadev.definex.advancedtaskmanager.controller;

import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.attachment.CreateAttachmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.attachment.AttachmentResponse;
import com.patikadev.definex.advancedtaskmanager.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute CreateAttachmentRequest request) {
        log.info("Request received to upload file {} for task ID: {}", file.getOriginalFilename(), request.getTaskId());
        AttachmentResponse response = attachmentService.uploadFile(request, file);
        log.info("File uploaded successfully with ID: {}", response.getId());
        return ResponseEntity.ok(ApiResponse.created(SuccessMessages.ATTACHMENT_UPLOADED, response));
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        log.info("Request received to download file with ID: {}", id);
        Resource resource = attachmentService.downloadFile(id);
        AttachmentResponse attachment = attachmentService.getAttachmentById(id);
        log.info("File downloaded successfully: {}", attachment.getFileName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<AttachmentResponse>> getAttachmentById(@PathVariable Long id) {
        log.info("Request received to get attachment details for ID: {}", id);
        AttachmentResponse response = attachmentService.getAttachmentById(id);
        log.info("Attachment details retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.ATTACHMENTS_FETCHED, response));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsByTaskId(@PathVariable UUID taskId) {
        log.info("Request received to get attachments for task ID: {}", taskId);
        List<AttachmentResponse> responses = attachmentService.getAttachmentsByTaskId(taskId);
        log.info("Retrieved {} attachments for task ID: {}", responses.size(), taskId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.ATTACHMENTS_FETCHED, responses));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsByUserId(@PathVariable UUID userId) {
        log.info("Request received to get attachments by user ID: {}", userId);
        List<AttachmentResponse> responses = attachmentService.getAttachmentsByUploadedUserId(userId);
        log.info("Retrieved {} attachments for user ID: {}", responses.size(), userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.ATTACHMENTS_FETCHED, responses));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long id) {
        log.info("Request received to delete attachment with ID: {}", id);
        attachmentService.deleteAttachment(id);
        log.info("Attachment deleted successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.ATTACHMENT_DELETED));
    }
} 