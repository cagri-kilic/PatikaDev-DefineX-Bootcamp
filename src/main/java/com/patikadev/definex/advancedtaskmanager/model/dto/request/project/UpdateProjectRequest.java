package com.patikadev.definex.advancedtaskmanager.model.dto.request.project;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UpdateProjectRequest {
    @Size(min = 2, max = 100, message = ValidationMessages.PROJECT_TITLE_SIZE)
    @Pattern(regexp = RegexPatterns.TITLE_PATTERN, message = ValidationMessages.PROJECT_TITLE_PATTERN)
    private String title;

    @Size(max = 1000, message = ValidationMessages.PROJECT_DESCRIPTION_MAX_SIZE)
    private String description;

    private Long departmentId;
    private Set<UUID> teamMemberIds;
} 