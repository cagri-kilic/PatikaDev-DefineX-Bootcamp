package com.patikadev.definex.advancedtaskmanager.model.dto.request.comment;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCommentRequest {
    @NotBlank(message = ValidationMessages.COMMENT_CONTENT_NOT_BLANK)
    private String content;

    @NotNull(message = ValidationMessages.COMMENT_TASK)
    private UUID taskId;
} 