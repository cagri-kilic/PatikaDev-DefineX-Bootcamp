package com.patikadev.definex.advancedtaskmanager.model.dto.response.department;

import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentDetailResponse extends DepartmentResponse {
    private Set<UserResponse> users;
    private Set<ProjectResponse> projects;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 