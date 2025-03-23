package com.patikadev.definex.advancedtaskmanager.model.dto.response.task;

import com.patikadev.definex.advancedtaskmanager.model.dto.response.comment.CommentResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.attachment.AttachmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.taskStateHistory.TaskStateHistoryResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDetailResponse extends TaskResponse {
    private Set<CommentResponse> comments;
    private Set<AttachmentResponse> attachments;
    private Set<TaskStateHistoryResponse> stateHistories;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
} 