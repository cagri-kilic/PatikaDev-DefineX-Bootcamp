package com.patikadev.definex.advancedtaskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.CreateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRolesRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_URL = "/api/auth";
    private static final String BASE_URL = "/api/users";
    private User adminUser;
    private User regularUser;
    private Department department;
    private String adminToken;
    private String userToken;
    private UUID adminUserId;
    private UUID regularUserId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        department = createDepartment();
        adminUser = createAdminUser();
        regularUser = createRegularUser();

        adminUserId = adminUser.getId();
        regularUserId = regularUser.getId();

        adminToken = getAuthToken(createLoginRequest(adminUser.getEmail(), "Password123!"));
        userToken = getAuthToken(createLoginRequest(regularUser.getEmail(), "Password123!"));
    }

    @Test
    @DisplayName("Create User - Valid Request - Returns User Response")
    void createUser_ValidRequest_ReturnsUserResponse() throws Exception {
        CreateUserRequest request = createUserRequest("new.user@example.com", UserRole.TEAM_MEMBER);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_CREATED))
                .andExpect(jsonPath("$.data.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(request.getLastName()))
                .andExpect(jsonPath("$.data.email").value(request.getEmail()))
                .andExpect(jsonPath("$.data.departmentId").value(request.getDepartmentId()))
                .andExpect(jsonPath("$.data.roles", hasItem(request.getRoles().iterator().next().name())));

        User savedUser = userRepository.findByEmailAndIsActiveTrue(request.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertTrue(passwordEncoder.matches(request.getPassword(), savedUser.getPassword()));
    }

    @Test
    @DisplayName("Create User - Unauthorized - Returns Forbidden")
    void createUser_Unauthorized_ReturnsForbidden() throws Exception {
        CreateUserRequest request = createUserRequest("new.user@example.com", UserRole.TEAM_MEMBER);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create User - Invalid Request - Returns Bad Request")
    void createUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateUserRequest request = createUserRequest("invalid-email", UserRole.TEAM_MEMBER);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Update User - Valid Request - Returns Updated User")
    void updateUser_ValidRequest_ReturnsUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setEmail("updated.user@example.com");
        request.setDepartmentId(department.getId());

        mockMvc.perform(put(BASE_URL + "/" + regularUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_UPDATED))
                .andExpect(jsonPath("$.data.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(request.getLastName()))
                .andExpect(jsonPath("$.data.email").value(request.getEmail()));

        User updatedUser = userRepository.findById(regularUserId).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(request.getFirstName(), updatedUser.getFirstName());
        assertEquals(request.getLastName(), updatedUser.getLastName());
        assertEquals(request.getEmail(), updatedUser.getEmail());
    }

    @Test
    @DisplayName("Update User - Self Update - Returns Updated User")
    void updateUser_SelfUpdate_ReturnsUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Self");
        request.setLastName("Updated");

        mockMvc.perform(put(BASE_URL + "/" + regularUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_UPDATED))
                .andExpect(jsonPath("$.data.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(request.getLastName()));
    }

    @Test
    @DisplayName("Update User - Unauthorized - Returns Forbidden")
    void updateUser_Unauthorized_ReturnsForbidden() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("User");

        mockMvc.perform(put(BASE_URL + "/" + adminUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update User Roles - Valid Request - Returns Updated User")
    void updateUserRoles_ValidRequest_ReturnsUpdatedUser() throws Exception {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(Set.of(UserRole.TEAM_LEADER, UserRole.PROJECT_MANAGER));

        mockMvc.perform(put(BASE_URL + "/" + regularUserId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_ROLES_UPDATED))
                .andExpect(jsonPath("$.data.roles", hasItems("TEAM_LEADER", "PROJECT_MANAGER")));

        User updatedUser = userRepository.findById(regularUserId).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(2, updatedUser.getRoles().size());
        assertTrue(updatedUser.getRoles().stream().anyMatch(role -> role.getName() == UserRole.TEAM_LEADER));
        assertTrue(updatedUser.getRoles().stream().anyMatch(role -> role.getName() == UserRole.PROJECT_MANAGER));
    }

    @Test
    @DisplayName("Update User Roles - Unauthorized - Returns Forbidden")
    void updateUserRoles_Unauthorized_ReturnsForbidden() throws Exception {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(Set.of(UserRole.TEAM_LEADER));

        mockMvc.perform(put(BASE_URL + "/" + regularUserId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get User By Id - Admin Access - Returns User Response")
    void getUserById_AdminAccess_ReturnsUserResponse() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + regularUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_FETCHED))
                .andExpect(jsonPath("$.data.id").value(regularUserId.toString()))
                .andExpect(jsonPath("$.data.firstName").value(regularUser.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(regularUser.getLastName()))
                .andExpect(jsonPath("$.data.email").value(regularUser.getEmail()));
    }

    @Test
    @DisplayName("Get User By Id - Self Access - Returns User Response")
    void getUserById_SelfAccess_ReturnsUserResponse() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + regularUserId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_FETCHED))
                .andExpect(jsonPath("$.data.id").value(regularUserId.toString()));
    }

    @Test
    @DisplayName("Get User By Id - Unauthorized - Returns Forbidden")
    void getUserById_Unauthorized_ReturnsForbidden() throws Exception {
        UUID otherUserId = UUID.randomUUID();

        mockMvc.perform(get(BASE_URL + "/" + otherUserId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get User By Email - Admin Access - Returns User Response")
    void getUserByEmail_AdminAccess_ReturnsUserResponse() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-email/" + regularUser.getEmail())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_FETCHED))
                .andExpect(jsonPath("$.data.email").value(regularUser.getEmail()));
    }

    @Test
    @DisplayName("Get User By Email - Self Access - Returns User Response")
    void getUserByEmail_SelfAccess_ReturnsUserResponse() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-email/" + regularUser.getEmail())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_FETCHED))
                .andExpect(jsonPath("$.data.email").value(regularUser.getEmail()));
    }

    @Test
    @DisplayName("Get User By Email - Unauthorized - Returns Forbidden")
    void getUserByEmail_Unauthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-email/other.user@example.com")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get All Users - Admin Access - Returns User List")
    void getAllUsers_AdminAccess_ReturnsUserList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USERS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[*].email", hasItems(adminUser.getEmail(), regularUser.getEmail())));
    }

    @Test
    @DisplayName("Get All Users - Unauthorized - Returns Forbidden")
    void getAllUsers_Unauthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Users By Department Id - Admin Access - Returns User List")
    void getUsersByDepartmentId_AdminAccess_ReturnsUserList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-department/" + department.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USERS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[*].departmentId", everyItem(is(department.getId().intValue()))));
    }

    @Test
    @DisplayName("Get Users By Role - Admin Access - Returns User List")
    void getUsersByRole_AdminAccess_ReturnsUserList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-role/ADMIN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USERS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[*].roles[*]", hasItem("ADMIN")));
    }

    @Test
    @DisplayName("Delete User - Admin Access - Success")
    void deleteUser_AdminAccess_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + regularUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_DELETED));

        User deletedUser = userRepository.findById(regularUserId).orElse(null);
        assertNotNull(deletedUser);
        assertFalse(deletedUser.getIsActive());
    }

    @Test
    @DisplayName("Delete User - Self Delete - Success")
    void deleteUser_SelfDelete_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + regularUserId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.USER_DELETED));

        User deletedUser = userRepository.findById(regularUserId).orElse(null);
        assertNotNull(deletedUser);
        assertFalse(deletedUser.getIsActive());
    }

    @Test
    @DisplayName("Delete User - Unauthorized - Returns Forbidden")
    void deleteUser_Unauthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + adminUserId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    private Department createDepartment() {
        Department dept = new Department();
        dept.setName("IT Department");
        dept.setDescription("Information Technology Department");
        dept.setIsActive(true);
        return departmentRepository.save(dept);
    }

    private User createAdminUser() {
        Role adminRole = roleRepository.findByName(UserRole.ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(UserRole.ADMIN);
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        User user = new User();
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setEmail("admin@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRoles(roles);
        user.setDepartment(department);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    private User createRegularUser() {
        Role role = roleRepository.findByName(UserRole.TEAM_MEMBER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(UserRole.TEAM_MEMBER);
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = new User();
        user.setFirstName("Regular");
        user.setLastName("User");
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRoles(roles);
        user.setDepartment(department);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private CreateUserRequest createUserRequest(String email, UserRole role) {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail(email);
        request.setPassword("Password123!");
        request.setRoles(Set.of(role));
        request.setDepartmentId(department.getId());
        return request;
    }

    private String getAuthToken(LoginRequest loginRequest) {
        try {
            MvcResult result = mockMvc.perform(post(AUTH_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();

            String responseContent = result.getResponse().getContentAsString();
            ApiResponse<AuthResponse> response = objectMapper.readValue(responseContent,
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, AuthResponse.class));

            return response.getData().getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get auth token", e);
        }
    }
} 