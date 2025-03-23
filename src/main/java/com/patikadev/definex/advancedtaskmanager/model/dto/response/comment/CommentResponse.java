package com.patikadev.definex.advancedtaskmanager.model.dto.response.comment;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private UUID taskId;
    private String taskTitle;
    private UUID userId;
    private String userName;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private boolean active;
} 