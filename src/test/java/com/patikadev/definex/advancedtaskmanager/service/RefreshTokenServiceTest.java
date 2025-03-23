package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.config.ApplicationProperties;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.exception.TokenRefreshException;
import com.patikadev.definex.advancedtaskmanager.model.entity.RefreshToken;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.repository.RefreshTokenRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ApplicationProperties.Security security;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;
    private RefreshToken refreshToken;
    private UUID userId;
    private String tokenValue;
    private long refreshTokenDuration;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tokenValue = UUID.randomUUID().toString();
        refreshTokenDuration = 86400000L;

        user = createUser();
        refreshToken = createRefreshToken(1L, tokenValue, Instant.now().plusMillis(refreshTokenDuration));
    }

    @Test
    @DisplayName("Create Refresh Token - Success")
    void createRefreshToken_Success() {
        when(applicationProperties.getSecurity()).thenReturn(security);
        when(security.getTokenRefreshExpirationMs()).thenReturn(refreshTokenDuration);
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        assertNotNull(result);
        assertEquals(refreshToken, result);
        assertEquals(user, result.getUser());
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(refreshTokenRepository).deactivateAllUserTokens(userId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Create Refresh Token - User Not Found")
    void createRefreshToken_UserNotFound() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> refreshTokenService.createRefreshToken(userId));

        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(refreshTokenRepository, never()).deactivateAllUserTokens(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Verify Expiration - Token Valid")
    void verifyExpiration_TokenValid() {
        RefreshToken validToken = createRefreshToken(2L, UUID.randomUUID().toString(), Instant.now().plusMillis(1000000));

        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        assertNotNull(result);
        assertEquals(validToken, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Verify Expiration - Token Expired")
    void verifyExpiration_TokenExpired() {
        RefreshToken expiredToken = createRefreshToken(3L, UUID.randomUUID().toString(), Instant.now().minusMillis(1000000));

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> refreshTokenService.verifyExpiration(expiredToken));

        assertNotNull(exception);
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("Find By Token - Token Found")
    void findByToken_TokenFound() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenValue);

        assertTrue(result.isPresent());
        assertEquals(refreshToken, result.get());
        verify(refreshTokenRepository).findByToken(tokenValue);
    }

    @Test
    @DisplayName("Find By Token - Token Not Found")
    void findByToken_TokenNotFound() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenValue);

        assertTrue(result.isEmpty());
        verify(refreshTokenRepository).findByToken(tokenValue);
    }

    @Test
    @DisplayName("Delete By User Id - Success")
    void deleteByUserId_Success() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));

        refreshTokenService.deleteByUserId(userId);

        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("Delete By User Id - User Not Found")
    void deleteByUserId_UserNotFound() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> refreshTokenService.deleteByUserId(userId));

        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    private User createUser() {
        User user = User.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .password("Password123!")
                .build();
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        return user;
    }

    private RefreshToken createRefreshToken(Long id, String token, Instant expiryDate) {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(id)
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .build();
        refreshToken.setIsActive(true);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setUpdatedAt(LocalDateTime.now());
        return refreshToken;
    }
} 