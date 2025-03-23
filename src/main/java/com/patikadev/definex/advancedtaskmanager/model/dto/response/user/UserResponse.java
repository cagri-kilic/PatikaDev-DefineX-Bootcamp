package com.patikadev.definex.advancedtaskmanager.model.dto.response.user;

import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<UserRole> roles;
    private Long departmentId;
    private String departmentName;
    private boolean active;
} 