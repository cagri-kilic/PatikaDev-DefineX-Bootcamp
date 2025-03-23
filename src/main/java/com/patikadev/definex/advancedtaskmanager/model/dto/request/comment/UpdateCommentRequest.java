package com.patikadev.definex.advancedtaskmanager.model.dto.request.comment;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCommentRequest {
    @NotBlank(message = ValidationMessages.COMMENT_CONTENT_NOT_BLANK)
    private String content;
} 