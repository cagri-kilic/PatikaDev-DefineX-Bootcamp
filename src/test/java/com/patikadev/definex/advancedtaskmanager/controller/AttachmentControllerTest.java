package com.patikadev.definex.advancedtaskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patikadev.definex.advancedtaskmanager.constant.FileConstants;
import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.LoginRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.attachment.CreateAttachmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.auth.AuthResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Attachment;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.AttachmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.ProjectRepository;
import com.patikadev.definex.advancedtaskmanager.repository.RoleRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${application.file.upload-dir}")
    private String uploadDir;

    private static final String AUTH_URL = "/api/auth";
    private static final String BASE_URL = "/api/attachments";
    private User adminUser;
    private User regularUser;
    private Department testDepartment;
    private Project testProject;
    private Task testTask;
    private Attachment testAttachment;
    private String adminToken;
    private String userToken;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() throws IOException {
        attachmentRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        Files.createDirectories(Paths.get(uploadDir));

        testDepartment = createTestDepartment();
        adminUser = createUser("admin@example.com", UserRole.ADMIN);
        regularUser = createUser("user@example.com", UserRole.TEAM_MEMBER);

        testProject = createTestProject();
        testTask = createTestTask();

        Path attachmentDir = Paths.get(uploadDir, FileConstants.TASK_ATTACHMENTS_DIR, testTask.getId().toString());
        Files.createDirectories(attachmentDir);

        String uniqueFileName = "123456_test.pdf";
        Path testFilePath = attachmentDir.resolve(uniqueFileName);
        Files.write(testFilePath, "Test file content".getBytes());

        testAttachment = createTestAttachment(testFilePath);

        adminToken = getAuthToken(createLoginRequest(adminUser.getEmail(), "Password123!"));
        userToken = getAuthToken(createLoginRequest(regularUser.getEmail(), "Password123!"));

        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Test file content".getBytes()
        );
    }

    @Test
    @DisplayName("Upload File - Valid Request - Returns Attachment Response")
    void uploadFile_ValidRequest_ReturnsAttachmentResponse() throws Exception {
        CreateAttachmentRequest request = new CreateAttachmentRequest();
        request.setTaskId(testTask.getId());

        mockMvc.perform(MockMvcRequestBuilders.multipart(BASE_URL + "/upload")
                        .file(testFile)
                        .param("taskId", testTask.getId().toString())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(SuccessMessages.ATTACHMENT_UPLOADED))
                .andExpect(jsonPath("$.data.fileName").value(testFile.getOriginalFilename()))
                .andExpect(jsonPath("$.data.contentType").value(testFile.getContentType()))
                .andExpect(jsonPath("$.data.taskId").value(testTask.getId().toString()))
                .andExpect(jsonPath("$.data.uploadedByUserId").value(regularUser.getId().toString()));
    }

    @Test
    @DisplayName("Upload File - Invalid Request - Returns Bad Request")
    void uploadFile_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart(BASE_URL + "/upload")
                        .file(testFile)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Upload File - Unauthenticated - Returns Unauthorized")
    void uploadFile_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart(BASE_URL + "/upload")
                        .file(testFile)
                        .param("taskId", testTask.getId().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get Attachment By Id - Valid Id - Returns Attachment Response")
    void getAttachmentById_ValidId_ReturnsAttachmentResponse() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testAttachment.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.ATTACHMENTS_FETCHED))
                .andExpect(jsonPath("$.data.id").value(testAttachment.getId()))
                .andExpect(jsonPath("$.data.fileName").value(testAttachment.getFileName()))
                .andExpect(jsonPath("$.data.taskId").value(testTask.getId().toString()));
    }

    @Test
    @DisplayName("Get Attachment By Id - Invalid Id - Returns Not Found")
    void getAttachmentById_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get Attachment By Id - Unauthenticated - Returns Unauthorized")
    void getAttachmentById_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testAttachment.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Download File - Valid Id - Returns File")
    void downloadFile_ValidId_ReturnsFile() throws Exception {
        mockMvc.perform(get(BASE_URL + "/download/" + testAttachment.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + testAttachment.getFileName() + "\""))
                .andExpect(content().contentType(MediaType.parseMediaType(testAttachment.getContentType())));
    }

    @Test
    @DisplayName("Download File - Invalid Id - Returns Not Found")
    void downloadFile_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/download/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Download File - Unauthenticated - Returns Unauthorized")
    void downloadFile_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/download/" + testAttachment.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get Attachments By Task Id - Valid Task Id - Returns Attachment List")
    void getAttachmentsByTaskId_ValidTaskId_ReturnsAttachmentList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/task/" + testTask.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.ATTACHMENTS_FETCHED))
                .andExpect(jsonPath("$.data[0].fileName").value(testAttachment.getFileName()));
    }

    @Test
    @DisplayName("Get Attachments By Task Id - Invalid Task Id - Returns Not Found")
    void getAttachmentsByTaskId_InvalidTaskId_ReturnsNotFound() throws Exception {
        UUID randomTaskId = UUID.randomUUID();
        mockMvc.perform(get(BASE_URL + "/task/" + randomTaskId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.httpStatus").value(404));
    }

    @Test
    @DisplayName("Get Attachments By User Id - Admin Access - Returns Attachment List")
    void getAttachmentsByUserId_AdminAccess_ReturnsAttachmentList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.ATTACHMENTS_FETCHED))
                .andExpect(jsonPath("$.data[0].fileName").value(testAttachment.getFileName()));
    }

    @Test
    @DisplayName("Get Attachments By User Id - Regular User Access - Returns Forbidden")
    void getAttachmentsByUserId_RegularUserAccess_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/user/" + adminUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete Attachment - Valid Id - Success")
    void deleteAttachment_ValidId_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testAttachment.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SuccessMessages.ATTACHMENT_DELETED));

        Attachment deletedAttachment = attachmentRepository.findById(testAttachment.getId()).orElse(null);
        assertNotNull(deletedAttachment);
        assertFalse(deletedAttachment.getIsActive());
    }

    @Test
    @DisplayName("Delete Attachment - Invalid Id - Returns Not Found")
    void deleteAttachment_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete Attachment - Unauthenticated - Returns Unauthorized")
    void deleteAttachment_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testAttachment.getId()))
                .andExpect(status().isUnauthorized());
    }

    private Department createTestDepartment() {
        Department department = new Department();
        department.setName("Test Department");
        department.setDescription("Test Department Description");
        department.setIsActive(true);
        return departmentRepository.save(department);
    }

    private Project createTestProject() {
        Project project = new Project();
        project.setTitle("Test Project");
        project.setDescription("Test Project Description");
        project.setStatus(ProjectStatus.PENDING);
        project.setDepartment(testDepartment);
        project.setIsActive(true);
        Set<User> teamMembers = new HashSet<>();
        teamMembers.add(regularUser);
        project.setTeamMembers(teamMembers);
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

    private Task createTestTask() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setUserStory("As a user I want to test attachments");
        task.setAcceptanceCriteria("The test should pass");
        task.setPriority(TaskPriority.MEDIUM);
        task.setState(TaskState.IN_PROGRESS);
        task.setAssignedUser(regularUser);
        task.setProject(testProject);
        task.setIsActive(true);

        return taskRepository.save(task);
    }

    private Attachment createTestAttachment(Path testFilePath) {
        Attachment attachment = new Attachment();
        attachment.setFileName("test.pdf");

        String relativePath = FileConstants.TASK_ATTACHMENTS_DIR + "/" + testTask.getId() + "/" + testFilePath.getFileName();
        attachment.setFilePath(relativePath);

        attachment.setFileSize(1024L);
        attachment.setContentType("application/pdf");
        attachment.setTask(testTask);
        attachment.setUploadedByUser(regularUser);
        attachment.setIsActive(true);

        return attachmentRepository.save(attachment);
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