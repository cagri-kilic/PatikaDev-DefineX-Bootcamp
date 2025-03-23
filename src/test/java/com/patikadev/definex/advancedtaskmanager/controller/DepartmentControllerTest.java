package com.patikadev.definex.advancedtaskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.CreateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.UpdateDepartmentRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_URL = "/api/auth";
    private static final String BASE_URL = "/api/departments";
    private Department testDepartment;
    private String adminToken;
    private String managerToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        testDepartment = createTestDepartment();
        User adminUser = createUser("admin@example.com", UserRole.ADMIN);
        User managerUser = createUser("manager@example.com", UserRole.PROJECT_GROUP_MANAGER);
        User regularUser = createUser("user@example.com", UserRole.TEAM_MEMBER);

        adminToken = getAuthToken(createLoginRequest(adminUser.getEmail(), "Password123!"));
        managerToken = getAuthToken(createLoginRequest(managerUser.getEmail(), "Password123!"));
        userToken = getAuthToken(createLoginRequest(regularUser.getEmail(), "Password123!"));
    }

    @Test
    @DisplayName("Create Department - Admin Access - Returns Department Response")
    void createDepartment_AdminAccess_ReturnsDepartmentResponse() throws Exception {
        CreateDepartmentRequest request = createDepartmentRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessages.DEPARTMENT_CREATED))
                .andExpect(jsonPath("$.data.name").value(request.getName()))
                .andExpect(jsonPath("$.data.description").value(request.getDescription()));

        Department savedDepartment = departmentRepository.findByNameAndIsActiveTrue(request.getName()).orElse(null);
        assertNotNull(savedDepartment);
        assertEquals(request.getName(), savedDepartment.getName());
        assertEquals(request.getDescription(), savedDepartment.getDescription());
    }

    @Test
    @DisplayName("Create Department - Project Group Manager Access - Returns Department Response")
    void createDepartment_ManagerAccess_ReturnsDepartmentResponse() throws Exception {
        CreateDepartmentRequest request = createDepartmentRequest();
        request.setName("HR Department");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create Department - Regular User Access - Returns Forbidden")
    void createDepartment_RegularUserAccess_ReturnsForbidden() throws Exception {
        CreateDepartmentRequest request = createDepartmentRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create Department - Invalid Request - Returns Bad Request")
    void createDepartment_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateDepartmentRequest request = new CreateDepartmentRequest();
        request.setName("I!");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Update Department - Admin Access - Returns Updated Department")
    void updateDepartment_AdminAccess_ReturnsUpdatedDepartment() throws Exception {
        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setName("Updated Department");
        request.setDescription("Updated Description");

        mockMvc.perform(put(BASE_URL + "/" + testDepartment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.DEPARTMENT_UPDATED))
                .andExpect(jsonPath("$.data.name").value(request.getName()))
                .andExpect(jsonPath("$.data.description").value(request.getDescription()));

        Department updatedDepartment = departmentRepository.findById(testDepartment.getId()).orElse(null);
        assertNotNull(updatedDepartment);
        assertEquals(request.getName(), updatedDepartment.getName());
        assertEquals(request.getDescription(), updatedDepartment.getDescription());
    }

    @Test
    @DisplayName("Update Department - Project Group Manager Access - Returns Updated Department")
    void updateDepartment_ManagerAccess_ReturnsUpdatedDepartment() throws Exception {
        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setName("Manager Updated Department");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update Department - Regular User Access - Returns Forbidden")
    void updateDepartment_RegularUserAccess_ReturnsForbidden() throws Exception {
        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setName("User Updated Department");

        mockMvc.perform(put(BASE_URL + "/" + testDepartment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Department By Id - Admin Access - Returns Department Detail")
    void getDepartmentById_AdminAccess_ReturnsDepartmentDetail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testDepartment.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.DEPARTMENTS_FETCHED))
                .andExpect(jsonPath("$.data.id").value(testDepartment.getId()))
                .andExpect(jsonPath("$.data.name").value(testDepartment.getName()))
                .andExpect(jsonPath("$.data.description").value(testDepartment.getDescription()));
    }

    @Test
    @DisplayName("Get Department By Id - Project Manager Access - Returns Department Detail")
    void getDepartmentById_ManagerAccess_ReturnsDepartmentDetail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testDepartment.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.DEPARTMENTS_FETCHED));
    }

    @Test
    @DisplayName("Get Department By Id - Regular User Access - Returns Forbidden")
    void getDepartmentById_RegularUserAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testDepartment.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Department By Name - Admin Access - Returns Department Detail")
    void getDepartmentByName_AdminAccess_ReturnsDepartmentDetail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-name/" + testDepartment.getName())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.DEPARTMENTS_FETCHED))
                .andExpect(jsonPath("$.data.name").value(testDepartment.getName()));
    }

    @Test
    @DisplayName("Get Department By Name - Project Manager Access - Returns Department Detail")
    void getDepartmentByName_ManagerAccess_ReturnsDepartmentDetail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-name/" + testDepartment.getName())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.DEPARTMENTS_FETCHED));
    }

    @Test
    @DisplayName("Get Department By Name - Regular User Access - Returns Forbidden")
    void getDepartmentByName_RegularUserAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/by-name/" + testDepartment.getName())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get All Departments - Authenticated User - Returns Department List")
    void getAllDepartments_AuthenticatedUser_ReturnsDepartmentList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.DEPARTMENTS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].name").isNotEmpty());
    }

    @Test
    @DisplayName("Get All Departments - Unauthenticated User - Returns Unauthorized")
    void getAllDepartments_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Delete Department - Admin Access - Success")
    void deleteDepartment_AdminAccess_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testDepartment.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.DEPARTMENT_DELETED));

        Department deletedDepartment = departmentRepository.findById(testDepartment.getId()).orElse(null);
        assertNotNull(deletedDepartment);
        assertFalse(deletedDepartment.getIsActive());
    }

    @Test
    @DisplayName("Delete Department - Non-Admin Access - Returns Forbidden")
    void deleteDepartment_NonAdminAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testDepartment.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    private Department createTestDepartment() {
        Department department = new Department();
        department.setName("IT Department");
        department.setDescription("Information Technology Department");
        department.setIsActive(true);
        return departmentRepository.save(department);
    }

    private User createUser(String email, UserRole userRole) {
        Role role = roleRepository.findByName(userRole)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(userRole);
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRoles(roles);
        user.setDepartment(testDepartment);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    private CreateDepartmentRequest createDepartmentRequest() {
        CreateDepartmentRequest request = new CreateDepartmentRequest();
        request.setName("Finance Department");
        request.setDescription("Finance Department Description");
        return request;
    }

    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
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