package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.attachment.CreateAttachmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.attachment.AttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AttachmentService {

    AttachmentResponse uploadFile(CreateAttachmentRequest request, MultipartFile file);

    Resource downloadFile(Long id);

    AttachmentResponse getAttachmentById(Long id);

    List<AttachmentResponse> getAttachmentsByTaskId(UUID taskId);

    List<AttachmentResponse> getAttachmentsByUploadedUserId(UUID userId);

    void deleteAttachment(Long id);
} 