package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.config.ApplicationProperties;
import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.constant.SecurityConstants;
import com.patikadev.definex.advancedtaskmanager.exception.TokenRefreshException;
import com.patikadev.definex.advancedtaskmanager.mapper.UserMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.RegisterRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.CreateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.RefreshToken;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.security.CustomUserDetails;
import com.patikadev.definex.advancedtaskmanager.security.jwt.JwtTokenProvider;
import com.patikadev.definex.advancedtaskmanager.service.AuthService;
import com.patikadev.definex.advancedtaskmanager.service.RefreshTokenService;
import com.patikadev.definex.advancedtaskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("authServiceImpl")
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationProperties applicationProperties;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(ErrorMessages.DUPLICATE_EMAIL);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_PASSWORD_MATCH);
        }

        CreateUserRequest createUserRequest = userMapper.toCreateUserRequest(request);
        UserResponse userResponse = userService.createUser(createUserRequest);

        Authentication authentication = authenticateUser(request.getEmail(), request.getPassword());

        String accessToken = generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userResponse.getId());

        return buildAuthResponse(accessToken, refreshToken, userResponse);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticateUser(request.getEmail(), request.getPassword());

        UserResponse userResponse = userService.getUserByEmail(request.getEmail());

        String accessToken = generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userResponse.getId());

        return buildAuthResponse(accessToken, refreshToken, userResponse);
    }

    @Override
    public UserResponse getCurrentUser() {
        String email = getCurrentUserEmail();
        return userService.getUserByEmail(email);
    }

    @Override
    public UUID getCurrentUserId() {
        String email = getCurrentUserEmail();
        User user = findUserByEmail(email);
        return user.getId();
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException(ErrorMessages.UNAUTHORIZED);
        }
        return authentication.getName();
    }

    @Override
    public boolean isResourceOwner(UUID resourceOwnerId) {
        return getCurrentUserId().equals(resourceOwnerId);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    if (!user.getIsActive()) {
                        throw new IllegalArgumentException(ErrorMessages.USER_INACTIVE);
                    }

                    CustomUserDetails customUserDetails = new CustomUserDetails(user);

                    String accessToken = tokenProvider.generateTokenFromUsername(
                            customUserDetails.getUsername(),
                            customUserDetails.getAuthorities()
                    );

                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    UserResponse userResponse = userMapper.toResponse(user);
                    return buildAuthResponse(accessToken, newRefreshToken, userResponse);
                })
                .orElseThrow(() -> new TokenRefreshException(ErrorMessages.INVALID_REFRESH_TOKEN));
    }

    @Override
    @Transactional
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = findUserByEmail(email);

            refreshTokenService.deleteByUserId(user.getId());
        }

        SecurityContextHolder.clearContext();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        ErrorMessages.RESOURCE_NOT_FOUND.formatted("User with email: " + email)));
    }

    private Authentication authenticateUser(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private String generateAccessToken(Authentication authentication) {
        return tokenProvider.generateTokenFromUsername(
                authentication.getName(),
                authentication.getAuthorities()
        );
    }

    private AuthResponse buildAuthResponse(String accessToken, RefreshToken refreshToken, UserResponse userResponse) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType(SecurityConstants.TOKEN_PREFIX.trim())
                .expiresIn(applicationProperties.getSecurity().getTokenExpirationMs() / 1000)
                .user(userResponse)
                .build();
    }
} 