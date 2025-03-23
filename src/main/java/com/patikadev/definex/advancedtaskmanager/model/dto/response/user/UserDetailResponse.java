package com.patikadev.definex.advancedtaskmanager.model.dto.response.user;

import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailResponse extends UserResponse {
    private Set<ProjectResponse> projects;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
} 