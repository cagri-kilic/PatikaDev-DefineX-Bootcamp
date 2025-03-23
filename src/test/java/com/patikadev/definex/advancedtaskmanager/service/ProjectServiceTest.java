package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.exception.UnauthorizedDepartmentAccessException;
import com.patikadev.definex.advancedtaskmanager.mapper.ProjectMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.CreateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectStatusRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.ProjectRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;
    private Department department;
    private User user;
    private CreateProjectRequest createProjectRequest;
    private UpdateProjectRequest updateProjectRequest;
    private UpdateProjectStatusRequest updateProjectStatusRequest;
    private ProjectResponse projectResponse;
    private ProjectDetailResponse projectDetailResponse;
    private List<Project> projectList;
    private List<ProjectResponse> projectResponseList;
    private Set<UUID> teamMemberIds;
    private final UUID projectId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final Long departmentId = 1L;
    private UserResponse adminUserResponse;
    private UserResponse projectManagerUserResponse;
    private UserResponse projectGroupManagerUserResponse;
    private UserResponse userWithOtherDepartment;

    @BeforeEach
    void setUp() {
        department = createDepartment();
        user = createUser();
        teamMemberIds = createTeamMemberIds();
        project = createProject();
        createProjectRequest = createCreateProjectRequest();
        updateProjectRequest = createUpdateProjectRequest();
        updateProjectStatusRequest = createUpdateProjectStatusRequest();
        projectResponse = createProjectResponse();
        projectDetailResponse = createProjectDetailResponse();
        projectList = createProjectList();
        projectResponseList = createProjectResponseList();

        adminUserResponse = createUserResponse("Admin", "User", "admin@example.com",
                new HashSet<>(Collections.singletonList(UserRole.ADMIN)), departmentId);
        projectManagerUserResponse = createUserResponse("PM", "User", "pm@example.com",
                new HashSet<>(Collections.singletonList(UserRole.PROJECT_MANAGER)), departmentId);
        projectGroupManagerUserResponse = createUserResponse("PGM", "User", "pgm@example.com",
                new HashSet<>(Collections.singletonList(UserRole.PROJECT_GROUP_MANAGER)), departmentId);
        userWithOtherDepartment = createUserResponse("Other", "User", "other@example.com",
                new HashSet<>(Collections.singletonList(UserRole.PROJECT_MANAGER)), departmentId + 1);
    }

    @Test
    @DisplayName("Create Project - Success as Project Manager of Same Department")
    void createProject_SuccessAsProjectManager() {
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(projectMapper.toEntity(any(CreateProjectRequest.class), any(Department.class), anySet()))
                .thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        ProjectResponse result = projectService.createProject(createProjectRequest);

        assertNotNull(result);
        assertEquals(projectResponse, result);
        verify(departmentRepository).findByIdAndIsActiveTrue(createProjectRequest.getDepartmentId());
        verify(projectMapper).toEntity(eq(createProjectRequest), eq(department), anySet());
        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Create Project - Unauthorized Department Access")
    void createProject_UnauthorizedDepartmentAccess() {
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedDepartmentAccessException.class, () -> projectService.createProject(createProjectRequest));
        verify(departmentRepository).findByIdAndIsActiveTrue(createProjectRequest.getDepartmentId());
        verify(authService).getCurrentUser();
        verify(projectMapper, never()).toEntity(any(), any(), any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Project - Success as Project Group Manager")
    void updateProject_SuccessAsProjectGroupManager() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);
        when(authService.getCurrentUser()).thenReturn(projectGroupManagerUserResponse);

        ProjectResponse result = projectService.updateProject(projectId, updateProjectRequest);

        assertNotNull(result);
        assertEquals(projectResponse, result);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(projectMapper).updateEntityFromDto(updateProjectRequest, project);
        verify(departmentRepository).findByIdAndIsActiveTrue(updateProjectRequest.getDepartmentId());
        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
    }

    @Test
    @DisplayName("Update Project - Unauthorized Access")
    void updateProject_UnauthorizedAccess() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedDepartmentAccessException.class, () -> projectService.updateProject(projectId, updateProjectRequest));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(authService).getCurrentUser();
        verify(projectMapper, never()).updateEntityFromDto(any(), any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add Team Member - Unauthorized Access")
    void addTeamMember_UnauthorizedAccess() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedDepartmentAccessException.class, () -> projectService.addTeamMember(projectId, userId));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(authService).getCurrentUser();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Remove Team Member - Unauthorized Access")
    void removeTeamMember_UnauthorizedAccess() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedDepartmentAccessException.class, () -> projectService.removeTeamMember(projectId, userId));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(authService).getCurrentUser();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Project Status - Unauthorized Access")
    void updateProjectStatus_UnauthorizedAccess() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedDepartmentAccessException.class,
                () -> projectService.updateProjectStatus(projectId, updateProjectStatusRequest));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(authService).getCurrentUser();
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete Project - Unauthorized Access")
    void deleteProject_UnauthorizedAccess() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedDepartmentAccessException.class, () -> projectService.deleteProject(projectId));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(authService).getCurrentUser();
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Project - Department Not Found")
    void createProject_DepartmentNotFound() {
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.createProject(createProjectRequest));
        verify(departmentRepository).findByIdAndIsActiveTrue(createProjectRequest.getDepartmentId());
        verify(projectMapper, never()).toEntity(any(), any(), any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Project By Id - Success")
    void getProjectById_Success() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(projectMapper.toDetailResponse(any(Project.class))).thenReturn(projectDetailResponse);

        ProjectDetailResponse result = projectService.getProjectById(projectId);

        assertNotNull(result);
        assertEquals(projectDetailResponse, result);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(projectMapper).toDetailResponse(project);
    }

    @Test
    @DisplayName("Get Project By Id - Not Found")
    void getProjectById_NotFound() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(projectId));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(projectMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Get All Projects - Success")
    void getAllProjects_Success() {
        when(projectRepository.findAllByIsActiveTrue()).thenReturn(projectList);
        when(projectMapper.toResponseList(anyList())).thenReturn(projectResponseList);

        List<ProjectResponse> result = projectService.getAllProjects();

        assertNotNull(result);
        assertEquals(projectResponseList, result);
        assertEquals(projectResponseList.size(), result.size());
        verify(projectRepository).findAllByIsActiveTrue();
        verify(projectMapper).toResponseList(projectList);
    }

    @Test
    @DisplayName("Get Projects By Department Id - Success")
    void getProjectsByDepartmentId_Success() {
        when(departmentRepository.existsByIdAndIsActiveTrue(anyLong())).thenReturn(true);
        when(projectRepository.findAllByDepartmentIdAndIsActiveTrue(anyLong())).thenReturn(projectList);
        when(projectMapper.toResponseList(anyList())).thenReturn(projectResponseList);

        List<ProjectResponse> result = projectService.getProjectsByDepartmentId(departmentId);

        assertNotNull(result);
        assertEquals(projectResponseList, result);
        assertEquals(projectResponseList.size(), result.size());
        verify(departmentRepository).existsByIdAndIsActiveTrue(departmentId);
        verify(projectRepository).findAllByDepartmentIdAndIsActiveTrue(departmentId);
        verify(projectMapper).toResponseList(projectList);
    }

    @Test
    @DisplayName("Get Projects By Department Id - Department Not Found")
    void getProjectsByDepartmentId_DepartmentNotFound() {
        when(departmentRepository.existsByIdAndIsActiveTrue(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectsByDepartmentId(departmentId));
        verify(departmentRepository).existsByIdAndIsActiveTrue(departmentId);
        verify(projectRepository, never()).findAllByDepartmentIdAndIsActiveTrue(any());
        verify(projectMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Get Projects By Status - Success")
    void getProjectsByStatus_Success() {
        when(projectRepository.findAllByStatusAndIsActiveTrue(any(ProjectStatus.class))).thenReturn(projectList);
        when(projectMapper.toResponseList(anyList())).thenReturn(projectResponseList);

        List<ProjectResponse> result = projectService.getProjectsByStatus(ProjectStatus.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(projectResponseList, result);
        assertEquals(projectResponseList.size(), result.size());
        verify(projectRepository).findAllByStatusAndIsActiveTrue(ProjectStatus.IN_PROGRESS);
        verify(projectMapper).toResponseList(projectList);
    }

    @Test
    @DisplayName("Get Projects By Team Member Id - Success")
    void getProjectsByTeamMemberId_Success() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(projectRepository.findAllByTeamMemberIdAndIsActiveTrue(any(UUID.class))).thenReturn(projectList);
        when(projectMapper.toResponseList(anyList())).thenReturn(projectResponseList);

        List<ProjectResponse> result = projectService.getProjectsByTeamMemberId(userId);

        assertNotNull(result);
        assertEquals(projectResponseList, result);
        assertEquals(projectResponseList.size(), result.size());
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(projectRepository).findAllByTeamMemberIdAndIsActiveTrue(userId);
        verify(projectMapper).toResponseList(projectList);
    }

    @Test
    @DisplayName("Get Projects By Team Member Id - User Not Found")
    void getProjectsByTeamMemberId_UserNotFound() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectsByTeamMemberId(userId));
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(projectRepository, never()).findAllByTeamMemberIdAndIsActiveTrue(any());
        verify(projectMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Exists By Id - True")
    void existsById_True() {
        when(projectRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);

        boolean result = projectService.existsById(projectId);

        assertTrue(result);
        verify(projectRepository).existsByIdAndIsActiveTrue(projectId);
    }

    @Test
    @DisplayName("Exists By Id - False")
    void existsById_False() {
        when(projectRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        boolean result = projectService.existsById(projectId);

        assertFalse(result);
        verify(projectRepository).existsByIdAndIsActiveTrue(projectId);
    }

    @Test
    @DisplayName("Update Project - Not Found")
    void updateProject_NotFound() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(projectId, updateProjectRequest));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(projectMapper, never()).updateEntityFromDto(any(), any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add Team Member - Success")
    void addTeamMember_Success() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        ProjectResponse result = projectService.addTeamMember(projectId, userId);

        assertNotNull(result);
        assertEquals(projectResponse, result);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Add Team Member - User Already In Project")
    void addTeamMember_UserAlreadyInProject() {
        User existingUser = createUser();
        project.getTeamMembers().add(existingUser);

        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(existingUser));
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        assertThrows(IllegalArgumentException.class, () -> projectService.addTeamMember(projectId, userId));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(projectRepository, never()).save(any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Remove Team Member - Success")
    void removeTeamMember_Success() {
        User existingUser = createUser();
        project.getTeamMembers().add(existingUser);

        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(existingUser));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        ProjectResponse result = projectService.removeTeamMember(projectId, userId);

        assertNotNull(result);
        assertEquals(projectResponse, result);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Remove Team Member - User Not In Project")
    void removeTeamMember_UserNotInProject() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        assertThrows(IllegalArgumentException.class, () -> projectService.removeTeamMember(projectId, userId));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(projectRepository, never()).save(any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Update Project Status - Success")
    void updateProjectStatus_Success() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        ProjectResponse result = projectService.updateProjectStatus(projectId, updateProjectStatusRequest);

        assertNotNull(result);
        assertEquals(projectResponse, result);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Delete Project - Success")
    void deleteProject_Success() {
        Project emptyProject = createProject();
        emptyProject.setTasks(new HashSet<>());

        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(emptyProject));
        when(projectRepository.save(any(Project.class))).thenReturn(emptyProject);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        projectService.deleteProject(projectId);

        assertFalse(emptyProject.getIsActive());
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(projectRepository).save(emptyProject);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Delete Project - Project Has Tasks")
    void deleteProject_ProjectHasTasks() {
        Set<Task> tasks = new HashSet<>();
        tasks.add(new Task());
        project.setTasks(tasks);

        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        assertThrows(IllegalStateException.class, () -> projectService.deleteProject(projectId));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(projectRepository, never()).save(any());
        verify(authService).getCurrentUser();
    }

    private Department createDepartment() {
        Department department = Department.builder()
                .id(departmentId)
                .name("IT Department")
                .description("Information Technology Department")
                .build();
        department.setIsActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        return department;
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
        return user;
    }

    private Project createProject() {
        Project project = Project.builder()
                .id(projectId)
                .title("Test Project")
                .description("Test Project Description")
                .status(ProjectStatus.PENDING)
                .department(department)
                .teamMembers(new HashSet<>())
                .tasks(new HashSet<>())
                .build();
        project.setIsActive(true);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }

    private Set<UUID> createTeamMemberIds() {
        Set<UUID> ids = new HashSet<>();
        ids.add(userId);
        return ids;
    }

    private CreateProjectRequest createCreateProjectRequest() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTitle("Test Project");
        request.setDescription("Test Project Description");
        request.setDepartmentId(departmentId);
        request.setTeamMemberIds(teamMemberIds);
        return request;
    }

    private UpdateProjectRequest createUpdateProjectRequest() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setTitle("Updated Project");
        request.setDescription("Updated Project Description");
        request.setDepartmentId(departmentId);
        request.setTeamMemberIds(teamMemberIds);
        return request;
    }

    private UpdateProjectStatusRequest createUpdateProjectStatusRequest() {
        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest();
        request.setNewStatus(ProjectStatus.IN_PROGRESS);
        return request;
    }

    private ProjectResponse createProjectResponse() {
        ProjectResponse response = new ProjectResponse();
        response.setId(projectId);
        response.setTitle("Test Project");
        response.setDescription("Test Project Description");
        response.setStatus(ProjectStatus.PENDING);
        response.setDepartmentId(departmentId);
        response.setDepartmentName("IT Department");
        response.setTotalTasks(0);
        response.setCompletedTasks(0);
        response.setActive(true);
        return response;
    }

    private ProjectDetailResponse createProjectDetailResponse() {
        ProjectDetailResponse response = new ProjectDetailResponse();
        response.setId(projectId);
        response.setTitle("Test Project");
        response.setDescription("Test Project Description");
        response.setStatus(ProjectStatus.PENDING);
        response.setDepartmentId(departmentId);
        response.setDepartmentName("IT Department");
        response.setTotalTasks(0);
        response.setCompletedTasks(0);
        response.setActive(true);
        response.setTeamMembers(new HashSet<>());
        response.setTasks(new HashSet<>());
        response.setCreatedAt(LocalDateTime.now());
        response.setCreatedBy("system");
        response.setUpdatedAt(LocalDateTime.now());
        response.setUpdatedBy("system");
        return response;
    }

    private UserResponse createUserResponse(String firstName, String lastName, String email,
                                            Set<UserRole> roles, Long departmentId) {
        UserResponse user = new UserResponse();
        user.setId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRoles(roles);
        user.setDepartmentId(departmentId);
        user.setActive(true);
        return user;
    }

    private List<Project> createProjectList() {
        return Collections.singletonList(project);
    }

    private List<ProjectResponse> createProjectResponseList() {
        return Collections.singletonList(projectResponse);
    }
} 