package com.patikadev.definex.advancedtaskmanager.model.dto.response.attachment;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private UUID taskId;
    private String taskTitle;
    private UUID uploadedByUserId;
    private String uploadedByUserName;
    private LocalDateTime createdAt;
    private boolean active;
} 