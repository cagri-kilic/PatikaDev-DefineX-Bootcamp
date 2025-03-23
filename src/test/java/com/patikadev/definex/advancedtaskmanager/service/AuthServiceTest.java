package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.config.ApplicationProperties;
import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.exception.TokenRefreshException;
import com.patikadev.definex.advancedtaskmanager.mapper.UserMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.RegisterRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.CreateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.RefreshToken;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.security.jwt.JwtTokenProvider;
import com.patikadev.definex.advancedtaskmanager.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ApplicationProperties.Security applicationSecurity;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserDetailResponse testUserDetailResponse;
    private RefreshToken testRefreshToken;
    private RegisterRequest testRegisterRequest;
    private LoginRequest testLoginRequest;
    private CreateUserRequest testCreateUserRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        createTestUser();
        createTestUserResponse();
        createTestUserDetailResponse();
        createTestRefreshToken();
        createTestRequests();

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Register - Success")
    void registerSuccess() {
        when(applicationProperties.getSecurity()).thenReturn(applicationSecurity);
        when(applicationSecurity.getTokenExpirationMs()).thenReturn(86400000L);

        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toCreateUserRequest(any(RegisterRequest.class))).thenReturn(testCreateUserRequest);
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUserResponse);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER")))
                .when(authentication).getAuthorities();

        doReturn("access-token")
                .when(tokenProvider).generateTokenFromUsername(anyString(), any());

        when(refreshTokenService.createRefreshToken(any(UUID.class))).thenReturn(testRefreshToken);

        AuthResponse response = authService.register(testRegisterRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(testUserResponse, response.getUser());

        verify(userService).existsByEmail("test@example.com");
        verify(userMapper).toCreateUserRequest(testRegisterRequest);
        verify(userService).createUser(testCreateUserRequest);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateTokenFromUsername(anyString(), any());
        verify(refreshTokenService).createRefreshToken(userId);
    }

    @Test
    @DisplayName("Register - Email Already Exists")
    void registerEmailAlreadyExists() {
        when(userService.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(testRegisterRequest));
        assertEquals(ErrorMessages.DUPLICATE_EMAIL, exception.getMessage());

        verify(userService).existsByEmail("test@example.com");
        verifyNoMoreInteractions(userMapper, userService, authenticationManager, tokenProvider, refreshTokenService);
    }

    @Test
    @DisplayName("Register - Password Mismatch")
    void registerPasswordMismatch() {
        testRegisterRequest.setConfirmPassword("DifferentPassword123!");
        when(userService.existsByEmail(anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(testRegisterRequest));
        assertEquals(ErrorMessages.INVALID_PASSWORD_MATCH, exception.getMessage());

        verify(userService).existsByEmail("test@example.com");
        verifyNoMoreInteractions(userMapper, userService, authenticationManager, tokenProvider, refreshTokenService);
    }

    @Test
    @DisplayName("Login - Success")
    void loginSuccess() {
        when(applicationProperties.getSecurity()).thenReturn(applicationSecurity);
        when(applicationSecurity.getTokenExpirationMs()).thenReturn(86400000L);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDetailResponse);
        when(authentication.getName()).thenReturn("test@example.com");

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER")))
                .when(authentication).getAuthorities();

        doReturn("access-token")
                .when(tokenProvider).generateTokenFromUsername(anyString(), any());

        when(refreshTokenService.createRefreshToken(any(UUID.class))).thenReturn(testRefreshToken);

        AuthResponse response = authService.login(testLoginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(testUserResponse.getId(), response.getUser().getId());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).getUserByEmail("test@example.com");
        verify(tokenProvider).generateTokenFromUsername(anyString(), any());
        verify(refreshTokenService).createRefreshToken(userId);
    }

    @Test
    @DisplayName("GetCurrentUser - Success")
    void getCurrentUserSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getUserByEmail(anyString())).thenReturn(testUserDetailResponse);

        UserResponse response = authService.getCurrentUser();

        assertNotNull(response);
        assertEquals(testUserDetailResponse, response);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("GetCurrentUser - Not Authenticated")
    void getCurrentUserNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.getCurrentUser());
        assertEquals(ErrorMessages.UNAUTHORIZED, exception.getMessage());

        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GetCurrentUserId - Success")
    void getCurrentUserIdSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(testUser));

        UUID id = authService.getCurrentUserId();

        assertEquals(userId, id);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmailAndIsActiveTrue("test@example.com");
    }

    @Test
    @DisplayName("IsResourceOwner - True")
    void isResourceOwnerTrue() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(testUser));

        boolean result = authService.isResourceOwner(userId);

        assertTrue(result);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmailAndIsActiveTrue("test@example.com");
    }

    @Test
    @DisplayName("IsResourceOwner - False")
    void isResourceOwnerFalse() {
        UUID differentUserId = UUID.randomUUID();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(testUser));

        boolean result = authService.isResourceOwner(differentUserId);

        assertFalse(result);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmailAndIsActiveTrue("test@example.com");
    }

    @Test
    @DisplayName("RefreshToken - Success")
    void refreshTokenSuccess() {
        when(applicationProperties.getSecurity()).thenReturn(applicationSecurity);
        when(applicationSecurity.getTokenExpirationMs()).thenReturn(86400000L);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .id(2L)
                .token("new-refresh-token")
                .user(testUser)
                .expiryDate(Instant.now().plusMillis(604800000))
                .build();
        newRefreshToken.setIsActive(true);

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(testRefreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(testRefreshToken);

        doReturn("new-access-token")
                .when(tokenProvider).generateTokenFromUsername(anyString(), any());

        when(refreshTokenService.createRefreshToken(any(UUID.class))).thenReturn(newRefreshToken);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        AuthResponse response = authService.refreshToken("refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals(testUserResponse, response.getUser());

        verify(refreshTokenService).findByToken("refresh-token");
        verify(refreshTokenService).verifyExpiration(testRefreshToken);
        verify(tokenProvider).generateTokenFromUsername(anyString(), any());
        verify(refreshTokenService).createRefreshToken(userId);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("RefreshToken - Not Found")
    void refreshTokenNotFound() {
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> authService.refreshToken("refresh-token"));
        assertEquals(ErrorMessages.INVALID_REFRESH_TOKEN, exception.getMessage());

        verify(refreshTokenService).findByToken("refresh-token");
        verifyNoMoreInteractions(refreshTokenService, tokenProvider, userMapper);
    }

    @Test
    @DisplayName("RefreshToken - User Inactive")
    void refreshTokenUserInactive() {
        testUser.setIsActive(false);
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(testRefreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(testRefreshToken);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.refreshToken("refresh-token"));
        assertEquals(ErrorMessages.USER_INACTIVE, exception.getMessage());

        verify(refreshTokenService).findByToken("refresh-token");
        verify(refreshTokenService).verifyExpiration(testRefreshToken);
        verifyNoMoreInteractions(tokenProvider, refreshTokenService, userMapper);
    }

    @Test
    @DisplayName("Logout - Success")
    void logoutSuccess() {
        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("test@example.com");
            when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(testUser));

            authService.logout();

            verify(securityContext).getAuthentication();
            verify(authentication).isAuthenticated();
            verify(authentication).getName();
            verify(userRepository).findByEmailAndIsActiveTrue("test@example.com");
            verify(refreshTokenService).deleteByUserId(userId);

            securityContextHolderMock.verify(SecurityContextHolder::clearContext);
        }
    }

    @Test
    @DisplayName("Logout - Not Authenticated")
    void logoutNotAuthenticated() {
        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            authService.logout();

            verify(securityContext).getAuthentication();
            verify(authentication).isAuthenticated();

            securityContextHolderMock.verify(SecurityContextHolder::clearContext);
            verifyNoInteractions(userRepository, refreshTokenService);
        }
    }

    @Test
    @DisplayName("FindUserByEmail - Not Found")
    void findUserByEmailNotFound() {
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.empty());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authService.getCurrentUserId());

        assertTrue(exception.getMessage().contains("User with email"));

        verify(userRepository).findByEmailAndIsActiveTrue("nonexistent@example.com");
    }

    private void createTestUser() {
        Set<Role> userRoles = createUserRoles();

        testUser = User.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("Password123!")
                .roles(userRoles)
                .build();
        testUser.setIsActive(true);
    }

    private Set<Role> createUserRoles() {
        Set<Role> userRoles = new HashSet<>();
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(UserRole.PROJECT_MANAGER);
        userRoles.add(userRole);
        return userRoles;
    }

    private void createTestUserResponse() {
        testUserResponse = new UserResponse();
        testUserResponse.setId(userId);
        testUserResponse.setFirstName("Test");
        testUserResponse.setLastName("User");
        testUserResponse.setEmail("test@example.com");
        testUserResponse.setRoles(Set.of(UserRole.PROJECT_MANAGER));
        testUserResponse.setActive(true);
    }

    private void createTestUserDetailResponse() {
        testUserDetailResponse = new UserDetailResponse();
        testUserDetailResponse.setId(userId);
        testUserDetailResponse.setFirstName("Test");
        testUserDetailResponse.setLastName("User");
        testUserDetailResponse.setEmail("test@example.com");
        testUserDetailResponse.setRoles(Set.of(UserRole.PROJECT_MANAGER));
        testUserDetailResponse.setActive(true);
        testUserDetailResponse.setCreatedAt(LocalDateTime.now());
        testUserDetailResponse.setCreatedBy("system");
    }

    private void createTestRefreshToken() {
        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token")
                .user(testUser)
                .expiryDate(Instant.now().plusMillis(604800000))
                .build();
        testRefreshToken.setIsActive(true);
    }

    private void createTestRequests() {
        createTestRegisterRequest();
        createTestLoginRequest();
        createTestCreateUserRequest();
    }

    private void createTestRegisterRequest() {
        testRegisterRequest = new RegisterRequest();
        testRegisterRequest.setFirstName("Test");
        testRegisterRequest.setLastName("User");
        testRegisterRequest.setEmail("test@example.com");
        testRegisterRequest.setPassword("Password123!");
        testRegisterRequest.setConfirmPassword("Password123!");
    }

    private void createTestLoginRequest() {
        testLoginRequest = new LoginRequest();
        testLoginRequest.setEmail("test@example.com");
        testLoginRequest.setPassword("Password123!");
    }

    private void createTestCreateUserRequest() {
        testCreateUserRequest = new CreateUserRequest();
        testCreateUserRequest.setFirstName("Test");
        testCreateUserRequest.setLastName("User");
        testCreateUserRequest.setEmail("test@example.com");
        testCreateUserRequest.setPassword("Password123!");
        testCreateUserRequest.setRoles(Set.of(UserRole.PROJECT_MANAGER));
    }
} 