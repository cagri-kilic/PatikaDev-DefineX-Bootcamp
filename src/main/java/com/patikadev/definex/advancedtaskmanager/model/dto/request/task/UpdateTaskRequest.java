package com.patikadev.definex.advancedtaskmanager.model.dto.request.task;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateTaskRequest {
    @Size(min = 2, max = 100, message = ValidationMessages.TASK_TITLE_SIZE)
    @Pattern(regexp = RegexPatterns.TITLE_PATTERN, message = ValidationMessages.TASK_TITLE_PATTERN)
    private String title;

    private String userStory;
    private String acceptanceCriteria;
    private TaskPriority priority;
    private UUID assignedUserId;
} 