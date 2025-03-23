package com.patikadev.definex.advancedtaskmanager.model.dto.response.department;

import lombok.Data;

@Data
public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
    private int totalUsers;
    private int totalProjects;
    private boolean active;
} 