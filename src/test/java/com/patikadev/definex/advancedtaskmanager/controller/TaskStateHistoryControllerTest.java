package com.patikadev.definex.advancedtaskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskStateRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.*;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.*;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskStateHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskStateHistoryRepository taskStateHistoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_URL = "/api/auth";
    private static final String BASE_URL = "/api/task-state-histories";
    private static final String TASKS_URL = "/api/tasks";

    private User adminUser;
    private Task testTask;
    private TaskStateHistory testTaskStateHistory;
    private Department testDepartment;
    private String adminToken;
    private String projectManagerToken;
    private String teamLeaderToken;
    private String teamMemberToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        taskStateHistoryRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        departmentRepository.deleteAll();

        testDepartment = createTestDepartment();
        adminUser = createUser("admin@example.com", UserRole.ADMIN);
        User projectManagerUser = createUser("manager@example.com", UserRole.PROJECT_MANAGER);
        User teamLeaderUser = createUser("leader@example.com", UserRole.TEAM_LEADER);
        User teamMemberUser = createUser("member@example.com", UserRole.TEAM_MEMBER);

        adminToken = getAuthToken(createLoginRequest(adminUser.getEmail(), "Password123!"));
        projectManagerToken = getAuthToken(createLoginRequest(projectManagerUser.getEmail(), "Password123!"));
        teamLeaderToken = getAuthToken(createLoginRequest(teamLeaderUser.getEmail(), "Password123!"));
        teamMemberToken = getAuthToken(createLoginRequest(teamMemberUser.getEmail(), "Password123!"));

        Project testProject = createTestProject();
        testTask = createTestTask(testProject);
        testTaskStateHistory = createTaskStateHistory(testTask, TaskState.BACKLOG, TaskState.IN_ANALYSIS, "Starting work", adminUser);
    }

    @Test
    @DisplayName("Get Task State History By Id - Admin Access - Returns History")
    void getTaskStateHistoryById_AdminAccess_ReturnsHistory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testTaskStateHistory.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_HISTORY_FETCHED))
                .andExpect(jsonPath("$.data.id").value(testTaskStateHistory.getId()))
                .andExpect(jsonPath("$.data.oldState").value(testTaskStateHistory.getOldState().toString()))
                .andExpect(jsonPath("$.data.newState").value(testTaskStateHistory.getNewState().toString()))
                .andExpect(jsonPath("$.data.reason").value(testTaskStateHistory.getReason()));
    }

    @Test
    @DisplayName("Get Task State History By Id - Project Manager Access - Returns History")
    void getTaskStateHistoryById_ProjectManagerAccess_ReturnsHistory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testTaskStateHistory.getId())
                        .header("Authorization", "Bearer " + projectManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_HISTORY_FETCHED))
                .andExpect(jsonPath("$.data.id").value(testTaskStateHistory.getId()));
    }

    @Test
    @DisplayName("Get Task State History By Id - Team Leader Access - Returns History")
    void getTaskStateHistoryById_TeamLeaderAccess_ReturnsHistory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testTaskStateHistory.getId())
                        .header("Authorization", "Bearer " + teamLeaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_HISTORY_FETCHED))
                .andExpect(jsonPath("$.data.id").value(testTaskStateHistory.getId()));
    }

    @Test
    @DisplayName("Get Task State History By Id - Team Member Access - Returns Forbidden")
    void getTaskStateHistoryById_TeamMemberAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testTaskStateHistory.getId())
                        .header("Authorization", "Bearer " + teamMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Task State Histories By Task Id - Admin Access - Returns List")
    void getTaskStateHistoriesByTaskId_AdminAccess_ReturnsList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/task/" + testTask.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_HISTORIES_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(testTaskStateHistory.getId()));
    }

    @Test
    @DisplayName("Get Task State Histories By Task Id - Team Member Access - Returns Forbidden")
    void getTaskStateHistoriesByTaskId_TeamMemberAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/task/" + testTask.getId())
                        .header("Authorization", "Bearer " + teamMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Task State Histories By User Id - Admin Access - Returns List")
    void getTaskStateHistoriesByUserId_AdminAccess_ReturnsList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/" + adminUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_HISTORIES_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(testTaskStateHistory.getId()));
    }

    @Test
    @DisplayName("Get Task State Histories By User Id - Team Member Access - Returns Forbidden")
    void getTaskStateHistoriesByUserId_TeamMemberAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/" + adminUser.getId())
                        .header("Authorization", "Bearer " + teamMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Task State Histories By Old State - Admin Access - Returns List")
    void getTaskStateHistoriesByOldState_AdminAccess_ReturnsList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/old-state/" + testTaskStateHistory.getOldState())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_HISTORIES_FETCHED))
                .andExpect(jsonPath("$.data[0].id").value(testTaskStateHistory.getId()))
                .andExpect(jsonPath("$.data[0].oldState").value(testTaskStateHistory.getOldState().toString()));
    }

    @Test
    @DisplayName("Get Task State Histories By Old State - Team Member Access - Returns Forbidden")
    void getTaskStateHistoriesByOldState_TeamMemberAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/old-state/" + testTaskStateHistory.getOldState())
                        .header("Authorization", "Bearer " + teamMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Task State Histories By New State - Admin Access - Returns List")
    void getTaskStateHistoriesByNewState_AdminAccess_ReturnsList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/new-state/" + testTaskStateHistory.getNewState())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_HISTORIES_FETCHED))
                .andExpect(jsonPath("$.data[0].id").value(testTaskStateHistory.getId()))
                .andExpect(jsonPath("$.data[0].newState").value(testTaskStateHistory.getNewState().toString()));
    }

    @Test
    @DisplayName("Get Task State Histories By New State - Team Member Access - Returns Forbidden")
    void getTaskStateHistoriesByNewState_TeamMemberAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/new-state/" + testTaskStateHistory.getNewState())
                        .header("Authorization", "Bearer " + teamMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Task State Histories By Date Range - Admin Access - Returns List")
    void getTaskStateHistoriesByDateRange_AdminAccess_ReturnsList() throws Exception {
        LocalDateTime startDate = testTaskStateHistory.getChangedAt().minusDays(1);
        LocalDateTime endDate = testTaskStateHistory.getChangedAt().plusDays(1);

        String startDateStr = startDate.format(DateTimeFormatter.ISO_DATE_TIME);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_DATE_TIME);

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("startDate", startDateStr)
                        .param("endDate", endDateStr)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_HISTORIES_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(testTaskStateHistory.getId()));
    }

    @Test
    @DisplayName("Get Task State Histories By Date Range - Team Member Access - Returns Forbidden")
    void getTaskStateHistoriesByDateRange_TeamMemberAccess_ReturnsForbidden() throws Exception {
        LocalDateTime startDate = testTaskStateHistory.getChangedAt().minusDays(1);
        LocalDateTime endDate = testTaskStateHistory.getChangedAt().plusDays(1);

        String startDateStr = startDate.format(DateTimeFormatter.ISO_DATE_TIME);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_DATE_TIME);

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("startDate", startDateStr)
                        .param("endDate", endDateStr)
                        .header("Authorization", "Bearer " + teamMemberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update Task State - Creates New Task State History")
    void updateTaskState_CreatesNewTaskStateHistory() throws Exception {
        UpdateTaskStateRequest updateRequest = new UpdateTaskStateRequest();
        updateRequest.setNewState(TaskState.COMPLETED);
        updateRequest.setReason("Task completed successfully");

        mockMvc.perform(patch(TASKS_URL + "/" + testTask.getId() + "/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + "/task/" + testTask.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].newState").value(TaskState.COMPLETED.toString()))
                .andExpect(jsonPath("$.data[0].reason").value("Task completed successfully"));
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
        user.setDepartment(testDepartment);
        user.setRoles(roles);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    private Project createTestProject() {
        Project project = new Project();
        project.setTitle("Test Project");
        project.setDescription("Test Project Description");
        project.setDepartment(testDepartment);
        project.setIsActive(true);
        return projectRepository.save(project);
    }

    private Department createTestDepartment() {
        Department department = new Department();
        department.setName("IT Department");
        department.setDescription("Information Technology Department");
        department.setIsActive(true);
        return departmentRepository.save(department);
    }

    private Task createTestTask(Project project) {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setUserStory("As a user, I want to test this functionality");
        task.setAcceptanceCriteria("The test passes");
        task.setState(TaskState.IN_PROGRESS);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(project);
        task.setIsActive(true);
        return taskRepository.save(task);
    }

    private TaskStateHistory createTaskStateHistory(Task task, TaskState oldState, TaskState newState, String reason, User changedBy) {
        TaskStateHistory history = new TaskStateHistory();
        history.setTask(task);
        history.setOldState(oldState);
        history.setNewState(newState);
        history.setReason(reason);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        return taskStateHistoryRepository.save(history);
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