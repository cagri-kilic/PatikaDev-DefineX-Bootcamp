package com.patikadev.definex.advancedtaskmanager.model.dto.response.project;

import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectDetailResponse extends ProjectResponse {
    private Set<UserResponse> teamMembers;
    private Set<TaskResponse> tasks;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
} 