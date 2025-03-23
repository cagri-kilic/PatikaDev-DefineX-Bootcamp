package com.patikadev.definex.advancedtaskmanager.model.dto.request.user;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRolesRequest {

    @NotEmpty(message = ValidationMessages.USER_ROLE_MUST_BE_SPECIFIED)
    private Set<UserRole> roles;
} 