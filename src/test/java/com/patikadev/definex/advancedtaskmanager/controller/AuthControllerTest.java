package com.patikadev.definex.advancedtaskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.RefreshTokenRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.RegisterRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.RefreshToken;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.RefreshTokenRepository;
import com.patikadev.definex.advancedtaskmanager.repository.RoleRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String BASE_URL = "/api/auth";
    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        testUser = createTestUser();
        registerRequest = createRegisterRequest();
        loginRequest = createLoginRequest();
    }

    @Test
    @DisplayName("Register - Valid Request - Returns Auth Response")
    void register_ValidRequest_ReturnsAuthResponse() throws Exception {
        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_REGISTERED))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.firstName").value(registerRequest.getFirstName()))
                .andExpect(jsonPath("$.data.user.lastName").value(registerRequest.getLastName()))
                .andExpect(jsonPath("$.data.user.email").value(registerRequest.getEmail()));

        User savedUser = userRepository.findByEmailAndIsActiveTrue(registerRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertTrue(passwordEncoder.matches(registerRequest.getPassword(), savedUser.getPassword()));
    }

    @Test
    @DisplayName("Register - Invalid Request - Returns Bad Request")
    void register_InvalidRequest_ReturnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = createRegisterRequest();
        invalidRequest.setEmail("invalid-email");

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Login - Valid Credentials - Returns Auth Response")
    void login_ValidCredentials_ReturnsAuthResponse() throws Exception {
        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.LOGIN_SUCCESS))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(loginRequest.getEmail()));
    }

    @Test
    @DisplayName("Login - Invalid Credentials - Returns Unauthorized")
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        LoginRequest invalidRequest = createLoginRequest();
        invalidRequest.setPassword("wrongpassword");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get Current User - Authenticated - Returns User Response")
    void getCurrentUser_Authenticated_ReturnsUserResponse() throws Exception {
        MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        ApiResponse<AuthResponse> apiResponse = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AuthResponse.class));

        accessToken = apiResponse.getData().getAccessToken();

        mockMvc.perform(get(BASE_URL + "/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_FETCHED))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));
    }

    @Test
    @DisplayName("Get Current User - Unauthenticated - Returns Unauthorized")
    void getCurrentUser_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh Token - Valid Token - Returns New Auth Response")
    void refreshToken_ValidToken_ReturnsNewAuthResponse() throws Exception {
        MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        ApiResponse<AuthResponse> apiResponse = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AuthResponse.class));

        refreshToken = apiResponse.getData().getRefreshToken();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);

        mockMvc.perform(post(BASE_URL + "/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TOKEN_REFRESHED))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken", not(refreshToken)));
    }

    @Test
    @DisplayName("Refresh Token - Invalid Token - Returns Forbidden")
    void refreshToken_InvalidToken_ReturnsBadRequest() throws Exception {
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("invalid-token");

        mockMvc.perform(post(BASE_URL + "/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Logout - Authenticated - Deletes Refresh Token And Clears Context")
    void logout_Authenticated_DeletesRefreshTokenAndClearsContext() throws Exception {
        MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        ApiResponse<AuthResponse> apiResponse = objectMapper.readValue(responseContent,
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AuthResponse.class));

        accessToken = apiResponse.getData().getAccessToken();
        refreshToken = apiResponse.getData().getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken).orElse(null);
        assertNotNull(storedToken);

        mockMvc.perform(post(BASE_URL + "/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.LOGOUT_SUCCESS));

        RefreshToken deletedToken = refreshTokenRepository.findByToken(refreshToken).orElse(null);
        assertNull(deletedToken);
    }

    @Test
    @DisplayName("Logout - Unauthenticated - Returns Unauthorized")
    void logout_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL + "/logout"))
                .andExpect(status().isUnauthorized());
    }

    private User createTestUser() {
        Role role = roleRepository.findByName(UserRole.PROJECT_MANAGER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(UserRole.PROJECT_MANAGER);
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRoles(roles);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    private RegisterRequest createRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("new.user@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");
        return request;
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        return request;
    }
} 