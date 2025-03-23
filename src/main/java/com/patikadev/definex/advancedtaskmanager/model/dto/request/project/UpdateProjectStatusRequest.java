package com.patikadev.definex.advancedtaskmanager.model.dto.request.project;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProjectStatusRequest {
    @NotNull(message = ValidationMessages.PROJECT_STATUS_MUST_BE_SPECIFIED)
    private ProjectStatus newStatus;
} 