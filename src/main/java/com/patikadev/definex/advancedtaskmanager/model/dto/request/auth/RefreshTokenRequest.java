package com.patikadev.definex.advancedtaskmanager.model.dto.request.auth;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = ValidationMessages.REFRESH_TOKEN_NOT_BLANK)
    private String refreshToken;
} 