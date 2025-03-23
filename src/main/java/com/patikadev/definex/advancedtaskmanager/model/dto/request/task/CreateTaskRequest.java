package com.patikadev.definex.advancedtaskmanager.model.dto.request.task;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateTaskRequest {
    @NotBlank(message = ValidationMessages.TASK_TITLE_NOT_BLANK)
    @Size(min = 2, max = 100, message = ValidationMessages.TASK_TITLE_SIZE)
    @Pattern(regexp = RegexPatterns.TITLE_PATTERN, message = ValidationMessages.TASK_TITLE_PATTERN)
    private String title;

    @NotBlank(message = ValidationMessages.USER_STORY_NOT_BLANK)
    private String userStory;

    @NotBlank(message = ValidationMessages.ACCEPTANCE_CRITERIA_NOT_BLANK)
    private String acceptanceCriteria;

    @NotNull(message = ValidationMessages.TASK_PRIORITY)
    private TaskPriority priority;

    @NotNull(message = ValidationMessages.TASK_PROJECT)
    private UUID projectId;

    private UUID assignedUserId;
} 