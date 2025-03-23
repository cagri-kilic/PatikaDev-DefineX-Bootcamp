package com.patikadev.definex.advancedtaskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.CreateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.UpdateCommentRequest;
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

import java.util.HashSet;
import java.util.List;
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
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_URL = "/api/auth";
    private static final String BASE_URL = "/api/comments";
    private User adminUser;
    private User regularUser;
    private Task testTask;
    private Comment testComment;
    private Department testDepartment;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        adminUser = createUser("admin@example.com", UserRole.ADMIN);
        regularUser = createUser("user@example.com", UserRole.TEAM_MEMBER);

        testDepartment = createDepartment();
        Project testProject = createProject();
        testTask = createTask(testProject);
        testComment = createComment(testTask, regularUser);

        adminToken = getAuthToken(createLoginRequest(adminUser.getEmail(), "Password123!"));
        userToken = getAuthToken(createLoginRequest(regularUser.getEmail(), "Password123!"));
    }

    @Test
    @DisplayName("Create Comment - Authenticated User - Returns Comment Response")
    void createComment_AuthenticatedUser_ReturnsCommentResponse() throws Exception {
        CreateCommentRequest request = createCommentRequest(testTask.getId());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessages.COMMENT_CREATED))
                .andExpect(jsonPath("$.data.content").value(request.getContent()))
                .andExpect(jsonPath("$.data.taskId").value(request.getTaskId().toString()))
                .andExpect(jsonPath("$.data.userId").value(regularUser.getId().toString()));

        List<Comment> comments = commentRepository.findAllByTaskIdWithDetails(testTask.getId());
        assertFalse(comments.isEmpty());
        assertTrue(comments.stream().anyMatch(c -> c.getContent().equals(request.getContent())));
    }

    @Test
    @DisplayName("Create Comment - Invalid Request - Returns Bad Request")
    void createComment_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Create Comment - Unauthenticated User - Returns Unauthorized")
    void createComment_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        CreateCommentRequest request = createCommentRequest(testTask.getId());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Update Comment - Comment Owner - Returns Updated Comment")
    void updateComment_CommentOwner_ReturnsUpdatedComment() throws Exception {
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("Updated comment content");

        mockMvc.perform(put(BASE_URL + "/" + testComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.COMMENT_UPDATED))
                .andExpect(jsonPath("$.data.content").value(request.getContent()))
                .andExpect(jsonPath("$.data.id").value(testComment.getId()));

        Comment updatedComment = commentRepository.findById(testComment.getId()).orElse(null);
        assertNotNull(updatedComment);
        assertEquals(request.getContent(), updatedComment.getContent());
    }

    @Test
    @DisplayName("Update Comment - Admin User - Returns Updated Comment")
    void updateComment_AdminUser_ReturnsUpdatedComment() throws Exception {
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("Admin updated comment");

        mockMvc.perform(put(BASE_URL + "/" + testComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.COMMENT_UPDATED))
                .andExpect(jsonPath("$.data.content").value(request.getContent()));
    }

    @Test
    @DisplayName("Update Comment - Invalid Request - Returns Bad Request")
    void updateComment_InvalidRequest_ReturnsBadRequest() throws Exception {
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("");

        mockMvc.perform(put(BASE_URL + "/" + testComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Get Comment By Id - Authenticated User - Returns Comment")
    void getCommentById_AuthenticatedUser_ReturnsComment() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testComment.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.COMMENTS_FETCHED))
                .andExpect(jsonPath("$.data.id").value(testComment.getId()))
                .andExpect(jsonPath("$.data.content").value(testComment.getContent()))
                .andExpect(jsonPath("$.data.taskId").value(testTask.getId().toString()))
                .andExpect(jsonPath("$.data.userId").value(regularUser.getId().toString()));
    }

    @Test
    @DisplayName("Get Comment By Id - Non-existent Comment - Returns Not Found")
    void getCommentById_NonExistentComment_ReturnsNotFound() throws Exception {
        Long nonExistentId = 999999L;

        mockMvc.perform(get(BASE_URL + "/" + nonExistentId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Get Comments By Task Id - Authenticated User - Returns Comment List")
    void getCommentsByTaskId_AuthenticatedUser_ReturnsCommentList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/task/" + testTask.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.COMMENTS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].taskId").value(testTask.getId().toString()));
    }

    @Test
    @DisplayName("Get Comments By User Id - Admin User - Returns Comment List")
    void getCommentsByUserId_AdminUser_ReturnsCommentList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.COMMENTS_FETCHED))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].userId").value(regularUser.getId().toString()));
    }

    @Test
    @DisplayName("Get Comments By User Id - Regular User - Returns Forbidden")
    void getCommentsByUserId_RegularUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/" + adminUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete Comment - Admin User - Success")
    void deleteComment_AdminUser_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testComment.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.COMMENT_DELETED));

        Comment deletedComment = commentRepository.findById(testComment.getId()).orElse(null);
        assertNotNull(deletedComment);
        assertFalse(deletedComment.getIsActive());
    }

    @Test
    @DisplayName("Delete Comment - Comment Owner - Success")
    void deleteComment_CommentOwner_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testComment.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.COMMENT_DELETED));

        Comment deletedComment = commentRepository.findById(testComment.getId()).orElse(null);
        assertNotNull(deletedComment);
        assertFalse(deletedComment.getIsActive());
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
        user.setIsActive(true);

        return userRepository.save(user);
    }

    private Project createProject() {
        Project project = new Project();
        project.setTitle("Test Project");
        project.setDescription("Test Project Description");
        project.setDepartment(testDepartment);
        project.setIsActive(true);
        return projectRepository.save(project);
    }

    private Department createDepartment() {
        Department department = new Department();
        department.setName("IT Department");
        department.setDescription("Information Technology Department");
        department.setIsActive(true);
        return departmentRepository.save(department);
    }

    private Task createTask(Project project) {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setUserStory("As a user, I want to test task functionality");
        task.setAcceptanceCriteria("The task should be testable");
        task.setState(TaskState.BACKLOG);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(project);
        task.setIsActive(true);
        return taskRepository.save(task);
    }

    private Comment createComment(Task task, User user) {
        Comment comment = new Comment();
        comment.setContent("Test comment content");
        comment.setTask(task);
        comment.setUser(user);
        comment.setIsActive(true);
        return commentRepository.save(comment);
    }

    private CreateCommentRequest createCommentRequest(UUID taskId) {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("New comment content");
        request.setTaskId(taskId);
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