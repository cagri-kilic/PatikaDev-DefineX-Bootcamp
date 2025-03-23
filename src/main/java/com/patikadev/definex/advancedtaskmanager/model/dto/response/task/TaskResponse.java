package com.patikadev.definex.advancedtaskmanager.model.dto.response.task;

import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import lombok.Data;

import java.util.UUID;

@Data
public class TaskResponse {
    private UUID id;
    private String title;
    private String userStory;
    private String acceptanceCriteria;
    private TaskState state;
    private TaskPriority priority;
    private String stateChangeReason;
    private UUID projectId;
    private String projectTitle;
    private UUID assignedUserId;
    private String assignedUserName;
    private boolean active;
    private int totalComments;
    private int totalAttachments;
} 