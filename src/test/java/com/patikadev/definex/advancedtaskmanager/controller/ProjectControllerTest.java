package com.patikadev.definex.advancedtaskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.CreateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectStatusRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.ProjectRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_URL = "/api/auth";
    private static final String BASE_URL = "/api/projects";
    private Department testDepartment;
    private Project testProject;
    private User managerUser;
    private User regularUser;
    private String adminToken;
    private String managerToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        testDepartment = createTestDepartment();

        User adminUser = createUser("admin@example.com", UserRole.ADMIN);
        managerUser = createUser("manager@example.com", UserRole.PROJECT_MANAGER);
        regularUser = createUser("user@example.com", UserRole.TEAM_MEMBER);

        adminToken = getAuthToken(createLoginRequest(adminUser.getEmail(), "Password123!"));
        managerToken = getAuthToken(createLoginRequest(managerUser.getEmail(), "Password123!"));
        userToken = getAuthToken(createLoginRequest(regularUser.getEmail(), "Password123!"));

        Set<User> teamMembers = new HashSet<>();
        teamMembers.add(managerUser);
        testProject = createTestProject(testDepartment, teamMembers);
    }

    @Test
    @DisplayName("Create Project - Admin Access - Returns Project Response")
    void createProject_AdminAccess_ReturnsProjectResponse() throws Exception {
        CreateProjectRequest request = createProjectRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECT_CREATED))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()))
                .andExpect(jsonPath("$.data.description").value(request.getDescription()))
                .andExpect(jsonPath("$.data.departmentId").value(request.getDepartmentId()));

        Project savedProject = projectRepository.findAllByIsActiveTrue().stream()
                .filter(p -> p.getTitle().equals(request.getTitle()))
                .findFirst()
                .orElse(null);
        assertNotNull(savedProject);
        assertEquals(request.getTitle(), savedProject.getTitle());
        assertEquals(request.getDescription(), savedProject.getDescription());
        assertEquals(request.getDepartmentId(), savedProject.getDepartment().getId());
    }

    @Test
    @DisplayName("Create Project - Project Manager Access - Returns Project Response")
    void createProject_ManagerAccess_ReturnsProjectResponse() throws Exception {
        CreateProjectRequest request = createProjectRequest();
        request.setTitle("Manager Project");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECT_CREATED))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()));
    }

    @Test
    @DisplayName("Create Project - Regular User Access - Returns Forbidden")
    void createProject_RegularUserAccess_ReturnsForbidden() throws Exception {
        CreateProjectRequest request = createProjectRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create Project - Invalid Request - Returns Bad Request")
    void createProject_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTitle("!");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Update Project - Admin Access - Returns Updated Project")
    void updateProject_AdminAccess_ReturnsUpdatedProject() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setTitle("Updated Project");
        request.setDescription("Updated Description");

        mockMvc.perform(put(BASE_URL + "/" + testProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECT_UPDATED))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()))
                .andExpect(jsonPath("$.data.description").value(request.getDescription()));

        Project updatedProject = projectRepository.findByIdAndIsActiveTrue(testProject.getId()).orElse(null);
        assertNotNull(updatedProject);
        assertEquals(request.getTitle(), updatedProject.getTitle());
        assertEquals(request.getDescription(), updatedProject.getDescription());
    }

    @Test
    @DisplayName("Update Project - Project Manager Access - Returns Updated Project")
    void updateProject_ManagerAccess_ReturnsUpdatedProject() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setTitle("Manager Updated Project");

        mockMvc.perform(put(BASE_URL + "/" + testProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECT_UPDATED))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()));
    }

    @Test
    @DisplayName("Update Project - Regular User Access - Returns Forbidden")
    void updateProject_RegularUserAccess_ReturnsForbidden() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setTitle("User Updated Project");

        mockMvc.perform(put(BASE_URL + "/" + testProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Project By Id - Project Manager Access - Returns Project Detail")
    void getProjectById_Project_Manager_Access_ReturnsProjectDetail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testProject.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECT_RETRIEVED))
                .andExpect(jsonPath("$.data.id").value(testProject.getId().toString()))
                .andExpect(jsonPath("$.data.title").value(testProject.getTitle()))
                .andExpect(jsonPath("$.data.description").value(testProject.getDescription()))
                .andExpect(jsonPath("$.data.teamMembers", hasSize(1)));
    }

    @Test
    @DisplayName("Get Project By Id - Unauthenticated User - Returns Unauthorized")
    void getProjectById_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testProject.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get All Projects - Admin Access - Returns Project List")
    void getAllProjects_Admin_Access_ReturnsProjectList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECTS_RETRIEVED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].title").isNotEmpty());
    }

    @Test
    @DisplayName("Get Projects By Department Id - Project Manager Access - Returns Project List")
    void getProjectsByDepartmentId_Project_Manager_Access_ReturnsProjectList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/department/" + testDepartment.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECTS_RETRIEVED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].departmentId").value(testDepartment.getId()));
    }

    @Test
    @DisplayName("Get Projects By Status - Admin Access - Returns Project List")
    void getProjectsByStatus_Admin_Access_ReturnsProjectList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/status/" + testProject.getStatus())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECTS_RETRIEVED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].status").value(testProject.getStatus().toString()));
    }

    @Test
    @DisplayName("Get Projects By Team Member - Admin Access - Returns Project List")
    void getProjectsByTeamMemberId_Admin_Access_ReturnsProjectList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/team-member/" + managerUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECTS_RETRIEVED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Add Team Member - Admin Access - Returns Updated Project")
    void addTeamMember_AdminAccess_ReturnsUpdatedProject() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + testProject.getId() + "/team-members/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TEAM_MEMBER_ADDED));

        Project updatedProject = projectRepository.findByIdAndIsActiveTrue(testProject.getId()).orElse(null);
        assertNotNull(updatedProject);
        assertTrue(updatedProject.getTeamMembers().stream()
                .anyMatch(user -> user.getId().equals(regularUser.getId())));
    }

    @Test
    @DisplayName("Add Team Member - Project Manager Access - Returns Updated Project")
    void addTeamMember_ManagerAccess_ReturnsUpdatedProject() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + testProject.getId() + "/team-members/" + regularUser.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TEAM_MEMBER_ADDED));
    }

    @Test
    @DisplayName("Add Team Member - Regular User Access - Returns Forbidden")
    void addTeamMember_RegularUserAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + testProject.getId() + "/team-members/" + regularUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Remove Team Member - Admin Access - Returns Updated Project")
    void removeTeamMember_AdminAccess_ReturnsUpdatedProject() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testProject.getId() + "/team-members/" + managerUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TEAM_MEMBER_REMOVED));

        Project updatedProject = projectRepository.findByIdAndIsActiveTrue(testProject.getId()).orElse(null);
        assertNotNull(updatedProject);
        assertFalse(updatedProject.getTeamMembers().stream()
                .anyMatch(user -> user.getId().equals(managerUser.getId())));
    }

    @Test
    @DisplayName("Update Project Status - Admin Access - Returns Updated Project")
    void updateProjectStatus_AdminAccess_ReturnsUpdatedProject() throws Exception {
        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest();
        request.setNewStatus(ProjectStatus.IN_PROGRESS);

        mockMvc.perform(patch(BASE_URL + "/" + testProject.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECT_STATUS_UPDATED))
                .andExpect(jsonPath("$.data.status").value(request.getNewStatus().toString()));

        Project updatedProject = projectRepository.findByIdAndIsActiveTrue(testProject.getId()).orElse(null);
        assertNotNull(updatedProject);
        assertEquals(request.getNewStatus(), updatedProject.getStatus());
    }

    @Test
    @DisplayName("Delete Project - Admin Access - Success")
    void deleteProject_AdminAccess_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testProject.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECT_DELETED));

        Project deletedProject = projectRepository.findByIdAndIsActiveTrue(testProject.getId()).orElse(null);
        assertNull(deletedProject);
    }

    @Test
    @DisplayName("Delete Project - Project Manager Access - Returns Success")
    void deleteProject_ManagerAccess_ReturnsSuccess() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testProject.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.PROJECT_DELETED));
    }

    @Test
    @DisplayName("Delete Project - Regular User Access - Returns Forbidden")
    void deleteProject_RegularUserAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testProject.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    private Department createTestDepartment() {
        Department department = new Department();
        department.setName("IT Department");
        department.setDescription("Information Technology Department");
        department.setIsActive(true);
        return departmentRepository.save(department);
    }

    private Project createTestProject(Department department, Set<User> teamMembers) {
        Project project = new Project();
        project.setTitle("Test Project");
        project.setDescription("Test Project Description");
        project.setStatus(ProjectStatus.PENDING);
        project.setDepartment(department);
        project.setTeamMembers(teamMembers);
        project.setIsActive(true);
        return projectRepository.save(project);
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

    private CreateProjectRequest createProjectRequest() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTitle("New Project");
        request.setDescription("New Project Description");
        request.setDepartmentId(testDepartment.getId());
        Set<UUID> teamMemberIds = new HashSet<>();
        teamMemberIds.add(managerUser.getId());
        request.setTeamMemberIds(teamMemberIds);
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