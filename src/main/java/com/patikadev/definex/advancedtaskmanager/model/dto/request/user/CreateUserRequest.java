package com.patikadev.definex.advancedtaskmanager.model.dto.request.user;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.SecurityConstants;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    @NotBlank(message = ValidationMessages.FIRST_NAME_NOT_BLANK)
    @Size(min = 2, max = 50, message = ValidationMessages.FIRST_NAME_SIZE)
    @Pattern(regexp = RegexPatterns.NAME_PATTERN, message = ValidationMessages.FIRST_NAME_PATTERN)
    private String firstName;

    @NotBlank(message = ValidationMessages.LAST_NAME_NOT_BLANK)
    @Size(min = 2, max = 50, message = ValidationMessages.LAST_NAME_SIZE)
    @Pattern(regexp = RegexPatterns.NAME_PATTERN, message = ValidationMessages.LAST_NAME_PATTERN)
    private String lastName;

    @NotBlank(message = ValidationMessages.EMAIL_NOT_BLANK)
    @Email(message = ValidationMessages.FIELD_EMAIL)
    private String email;

    @NotBlank(message = ValidationMessages.PASSWORD_NOT_BLANK)
    @Size(min = SecurityConstants.MIN_PASSWORD_LENGTH, message = ValidationMessages.PASSWORD_MIN_LENGTH)
    @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN, message = ValidationMessages.PASSWORD_PATTERN)
    private String password;

    private Set<UserRole> roles;

    private Long departmentId;
} 