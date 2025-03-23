package com.patikadev.definex.advancedtaskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.CreateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskStateRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.ProjectRepository;
import com.patikadev.definex.advancedtaskmanager.repository.RoleRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
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
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

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
    private static final String BASE_URL = "/api/tasks";
    private Task testTask;
    private Project testProject;
    private Department testDepartment;
    private User teamLeaderUser;
    private User regularUser;
    private String adminToken;
    private String teamLeaderToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        testDepartment = createTestDepartment();
        User adminUser = createUser("admin@example.com", UserRole.ADMIN);
        teamLeaderUser = createUser("leader@example.com", UserRole.TEAM_LEADER);
        regularUser = createUser("user@example.com", UserRole.TEAM_MEMBER);
        testProject = createTestProject();
        testTask = createTestTask();

        adminToken = getAuthToken(createLoginRequest(adminUser.getEmail(), "Password123!"));
        teamLeaderToken = getAuthToken(createLoginRequest(teamLeaderUser.getEmail(), "Password123!"));
        regularUserToken = getAuthToken(createLoginRequest(regularUser.getEmail(), "Password123!"));
    }

    @Test
    @DisplayName("Create Task - Admin Access - Returns Task Response")
    void createTask_AdminAccess_ReturnsTaskResponse() throws Exception {
        CreateTaskRequest request = createTaskRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_CREATED))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()))
                .andExpect(jsonPath("$.data.userStory").value(request.getUserStory()))
                .andExpect(jsonPath("$.data.acceptanceCriteria").value(request.getAcceptanceCriteria()))
                .andExpect(jsonPath("$.data.priority").value(request.getPriority().toString()))
                .andExpect(jsonPath("$.data.state").value("BACKLOG"))
                .andExpect(jsonPath("$.data.projectId").value(request.getProjectId().toString()));
    }

    @Test
    @DisplayName("Create Task - Team Leader Access - Returns Task Response")
    void createTask_TeamLeaderAccess_ReturnsTaskResponse() throws Exception {
        CreateTaskRequest request = createTaskRequest();
        request.setTitle("New Feature Task");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + teamLeaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_CREATED))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()));
    }

    @Test
    @DisplayName("Create Task - Regular User Access - Returns Forbidden")
    void createTask_RegularUserAccess_ReturnsForbidden() throws Exception {
        CreateTaskRequest request = createTaskRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create Task - Invalid Request - Returns Bad Request")
    void createTask_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("A");
        request.setPriority(TaskPriority.HIGH);
        request.setProjectId(testProject.getId());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Update Task - Admin Access - Returns Updated Task")
    void updateTask_AdminAccess_ReturnsUpdatedTask() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task Title");
        request.setUserStory("Updated user story");
        request.setPriority(TaskPriority.CRITICAL);

        mockMvc.perform(put(BASE_URL + "/" + testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_UPDATED))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()))
                .andExpect(jsonPath("$.data.userStory").value(request.getUserStory()))
                .andExpect(jsonPath("$.data.priority").value(request.getPriority().toString()));
    }

    @Test
    @DisplayName("Update Task - Team Leader Access - Returns Updated Task")
    void updateTask_TeamLeaderAccess_ReturnsUpdatedTask() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Leader Updated Task");
        request.setPriority(TaskPriority.HIGH);

        mockMvc.perform(put(BASE_URL + "/" + testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + teamLeaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_UPDATED))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()));
    }

    @Test
    @DisplayName("Update Task - Regular User Access - Returns Forbidden")
    void updateTask_RegularUserAccess_ReturnsForbidden() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("User Updated Task");

        mockMvc.perform(put(BASE_URL + "/" + testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get Task By Id - Authenticated User - Returns Task Detail")
    void getTaskById_AuthenticatedUser_ReturnsTaskDetail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testTask.getId())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASKS_FETCHED))
                .andExpect(jsonPath("$.data.id").value(testTask.getId().toString()))
                .andExpect(jsonPath("$.data.title").value(testTask.getTitle()))
                .andExpect(jsonPath("$.data.projectId").value(testProject.getId().toString()));
    }

    @Test
    @DisplayName("Get Task By Id - Unauthenticated User - Returns Unauthorized")
    void getTaskById_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testTask.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get All Tasks - Authenticated User - Returns Task List")
    void getAllTasks_AuthenticatedUser_ReturnsTaskList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASKS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].title").isNotEmpty());
    }

    @Test
    @DisplayName("Get Tasks By Project Id - Authenticated User - Returns Task List")
    void getTasksByProjectId_AuthenticatedUser_ReturnsTaskList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/project/" + testProject.getId())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASKS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].projectId").value(testProject.getId().toString()));
    }

    @Test
    @DisplayName("Get Tasks By Assigned User Id - Authenticated User - Returns Task List")
    void getTasksByAssignedUserId_AuthenticatedUser_ReturnsTaskList() throws Exception {
        testTask.setAssignedUser(regularUser);
        taskRepository.save(testTask);

        mockMvc.perform(get(BASE_URL + "/assigned-user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASKS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Get Tasks By State - Authenticated User - Returns Task List")
    void getTasksByState_AuthenticatedUser_ReturnsTaskList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/state/" + testTask.getState())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASKS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].state").value(testTask.getState().toString()));
    }

    @Test
    @DisplayName("Get Tasks By Priority - Authenticated User - Returns Task List")
    void getTasksByPriority_AuthenticatedUser_ReturnsTaskList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/priority/" + testTask.getPriority())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASKS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].priority").value(testTask.getPriority().toString()));
    }

    @Test
    @DisplayName("Update Task State - Authenticated User - Returns Updated Task")
    void updateTaskState_AuthenticatedUser_ReturnsUpdatedTask() throws Exception {
        UpdateTaskStateRequest request = new UpdateTaskStateRequest();
        request.setNewState(TaskState.IN_ANALYSIS);
        request.setReason("Starting the development");

        mockMvc.perform(patch(BASE_URL + "/" + testTask.getId() + "/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_STATE_UPDATED))
                .andExpect(jsonPath("$.data.state").value(request.getNewState().toString()))
                .andExpect(jsonPath("$.data.stateChangeReason").value(request.getReason()));
    }

    @Test
    @DisplayName("Update Task State - Authenticated User - Returns Bad Request")
    void updateTaskState_AuthenticatedUser_ReturnsBadRequest() throws Exception {
        UpdateTaskStateRequest request = new UpdateTaskStateRequest();
        request.setNewState(TaskState.COMPLETED);
        request.setReason("Trying to complete task directly from backlog");

        mockMvc.perform(patch(BASE_URL + "/" + testTask.getId() + "/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_STATE_TRANSITION.formatted(TaskState.BACKLOG, TaskState.COMPLETED)));
    }

    @Test
    @DisplayName("Assign Task To User - Team Leader Access - Returns Updated Task")
    void assignTaskToUser_TeamLeaderAccess_ReturnsUpdatedTask() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + testTask.getId() + "/assign/" + regularUser.getId())
                        .header("Authorization", "Bearer " + teamLeaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_ASSIGNED))
                .andExpect(jsonPath("$.data.assignedUserId").value(regularUser.getId().toString()));
    }

    @Test
    @DisplayName("Assign Task To User - Regular User Access - Returns Forbidden")
    void assignTaskToUser_RegularUserAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + testTask.getId() + "/assign/" + teamLeaderUser.getId())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unassign Task - Team Leader Access - Returns Updated Task")
    void unassignTask_TeamLeaderAccess_ReturnsUpdatedTask() throws Exception {
        testTask.setAssignedUser(regularUser);
        taskRepository.save(testTask);

        mockMvc.perform(post(BASE_URL + "/" + testTask.getId() + "/unassign")
                        .header("Authorization", "Bearer " + teamLeaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_UNASSIGNED))
                .andExpect(jsonPath("$.data.assignedUserId").isEmpty());
    }

    @Test
    @DisplayName("Delete Task - Admin Access - Success")
    void deleteTask_AdminAccess_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testTask.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.TASK_DELETED));

        Task deletedTask = taskRepository.findById(testTask.getId()).orElse(null);
        assertNotNull(deletedTask);
        assertFalse(deletedTask.getIsActive());
    }

    @Test
    @DisplayName("Delete Task - Regular User Access - Returns Forbidden")
    void deleteTask_RegularUserAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testTask.getId())
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    private Department createTestDepartment() {
        Department department = new Department();
        department.setName("IT Department");
        department.setDescription("Information Technology Department");
        department.setIsActive(true);
        return departmentRepository.save(department);
    }

    private Project createTestProject() {
        Project project = new Project();
        project.setTitle("Test Project");
        project.setDescription("Test Project Description");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setDepartment(testDepartment);
        project.setIsActive(true);
        Set<User> teamMembers = new HashSet<>();
        teamMembers.add(teamLeaderUser);
        teamMembers.add(regularUser);
        project.setTeamMembers(teamMembers);
        return projectRepository.save(project);
    }

    private Task createTestTask() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setUserStory("As a user, I want to test the task functionality");
        task.setAcceptanceCriteria("Task can be created and managed through API");
        task.setState(TaskState.BACKLOG);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(testProject);
        task.setIsActive(true);
        return taskRepository.save(task);
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

    private CreateTaskRequest createTaskRequest() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setUserStory("As a user, I want to create a new task");
        request.setAcceptanceCriteria("Task should be created with all required fields");
        request.setPriority(TaskPriority.HIGH);
        request.setProjectId(testProject.getId());
        request.setAssignedUserId(regularUser.getId());
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