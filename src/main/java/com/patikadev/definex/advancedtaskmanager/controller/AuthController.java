package com.patikadev.definex.advancedtaskmanager.controller;

import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.RefreshTokenRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.RegisterRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);
        log.info("User successfully registered with email: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.created(SuccessMessages.USER_REGISTERED, authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt received for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        log.info("User successfully logged in with email: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.LOGIN_SUCCESS, authResponse));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("Request received to get current user information");
        UserResponse userResponse = authService.getCurrentUser();
        log.info("Current user information retrieved for user ID: {}", userResponse.getId());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USER_FETCHED, userResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
        log.info("Token successfully refreshed");
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TOKEN_REFRESHED, authResponse));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request received");
        authService.logout();
        log.info("User successfully logged out");
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.LOGOUT_SUCCESS));
    }
} 