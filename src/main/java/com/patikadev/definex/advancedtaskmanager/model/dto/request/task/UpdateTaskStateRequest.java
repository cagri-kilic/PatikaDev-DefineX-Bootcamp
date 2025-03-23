package com.patikadev.definex.advancedtaskmanager.model.dto.request.task;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTaskStateRequest {
    @NotNull(message = ValidationMessages.TASK_STATE)
    private TaskState newState;

    @Size(max = 500, message = ValidationMessages.STATE_CHANGE_REASON_MAX_SIZE)
    private String reason;
} 