package com.patikadev.definex.advancedtaskmanager.model.dto.request.attachment;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateAttachmentRequest {
    @NotNull(message = ValidationMessages.FILE_TASK)
    private UUID taskId;
} 