package com.patikadev.definex.advancedtaskmanager.model.dto.response.project;

import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ProjectResponse {
    private UUID id;
    private String title;
    private String description;
    private ProjectStatus status;
    private Long departmentId;
    private String departmentName;
    private int totalTasks;
    private int completedTasks;
    private boolean active;
} 