package com.patikadev.definex.advancedtaskmanager.model.dto.request.auth;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
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
    @Size(min = 8, message = ValidationMessages.PASSWORD_MIN_LENGTH)
    @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN, message = ValidationMessages.PASSWORD_PATTERN)
    private String password;

    @NotBlank(message = ValidationMessages.PASSWORD_CONFIRMATION)
    private String confirmPassword;

    private Long departmentId;
} 