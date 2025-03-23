package com.patikadev.definex.advancedtaskmanager.model.dto.request.user;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 2, max = 50, message = ValidationMessages.FIRST_NAME_SIZE)
    @Pattern(regexp = RegexPatterns.NAME_PATTERN, message = ValidationMessages.FIRST_NAME_PATTERN)
    private String firstName;

    @Size(min = 2, max = 50, message = ValidationMessages.LAST_NAME_SIZE)
    @Pattern(regexp = RegexPatterns.NAME_PATTERN, message = ValidationMessages.LAST_NAME_PATTERN)
    private String lastName;

    @Email(message = ValidationMessages.FIELD_EMAIL)
    private String email;

    private Long departmentId;
} 