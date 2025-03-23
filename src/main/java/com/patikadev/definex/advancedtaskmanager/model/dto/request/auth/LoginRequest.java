package com.patikadev.definex.advancedtaskmanager.model.dto.request.auth;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = ValidationMessages.EMAIL_NOT_BLANK)
    private String email;

    @NotBlank(message = ValidationMessages.PASSWORD_NOT_BLANK)
    private String password;
} 