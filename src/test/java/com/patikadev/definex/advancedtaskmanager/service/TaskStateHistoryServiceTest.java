package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.exception.UnauthorizedTaskAccessException;
import com.patikadev.definex.advancedtaskmanager.mapper.TaskStateHistoryMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.taskStateHistory.TaskStateHistoryResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.TaskStateHistory;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskStateHistoryRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.impl.TaskStateHistoryServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskStateHistoryServiceTest {

    @Mock
    private TaskStateHistoryRepository taskStateHistoryRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private TaskStateHistoryMapper taskStateHistoryMapper;

    @InjectMocks
    private TaskStateHistoryServiceImpl taskStateHistoryService;

    private TaskStateHistory taskStateHistory;
    private Department department;
    private Project project;
    private Task task;
    private User user;
    private TaskStateHistoryResponse taskStateHistoryResponse;
    private List<TaskStateHistory> taskStateHistoryList;
    private List<TaskStateHistoryResponse> taskStateHistoryResponseList;
    private UUID taskId;
    private UUID userId;
    private Long taskStateHistoryId;
    private Long departmentId;
    private TaskState oldState;
    private TaskState newState;
    private LocalDateTime changedAt;
    private String reason;
    private UserResponse adminUserResponse;
    private UserResponse projectManagerUserResponse;
    private UserResponse userWithOtherDepartment;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();
        taskStateHistoryId = 1L;
        departmentId = 1L;
        oldState = TaskState.BACKLOG;
        newState = TaskState.IN_PROGRESS;
        changedAt = LocalDateTime.now();
        reason = "Task started";

        department = createDepartment();
        project = createProject();
        task = createTask();
        user = createUser();
        taskStateHistory = createTaskStateHistory();
        taskStateHistoryResponse = createTaskStateHistoryResponse();
        taskStateHistoryList = createTaskStateHistoryList();
        taskStateHistoryResponseList = createTaskStateHistoryResponseList();

        adminUserResponse = createUserResponse("Admin", "User", "admin@example.com",
                new HashSet<>(Collections.singletonList(UserRole.ADMIN)), departmentId);
        projectManagerUserResponse = createUserResponse("PM", "User", "pm@example.com",
                new HashSet<>(Collections.singletonList(UserRole.PROJECT_MANAGER)), departmentId);
        userWithOtherDepartment = createUserResponse("Other", "User", "other@example.com",
                new HashSet<>(Collections.singletonList(UserRole.PROJECT_MANAGER)), departmentId + 1);
    }

    @Test
    @DisplayName("Create Task State History - Success")
    void createTaskStateHistory_Success() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(taskStateHistoryMapper.toEntity(any(TaskState.class), any(TaskState.class), anyString(), any(LocalDateTime.class), any(User.class))).thenReturn(taskStateHistory);

        taskStateHistoryService.createTaskStateHistory(taskId, oldState, newState, changedAt, reason);

        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUserId();
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskStateHistoryMapper).toEntity(oldState, newState, reason, changedAt, user);
        verify(taskStateHistoryRepository).save(taskStateHistory);
    }

    @Test
    @DisplayName("Create Task State History - Task Not Found")
    void createTaskStateHistory_TaskNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskStateHistoryService.createTaskStateHistory(taskId, oldState, newState, changedAt, reason));

        assertTrue(exception.getMessage().contains(taskId.toString()));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService, never()).getCurrentUserId();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(taskStateHistoryMapper, never()).toEntity(any(), any(), any(), any(), any());
        verify(taskStateHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Task State History - User Not Found")
    void createTaskStateHistory_UserNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskStateHistoryService.createTaskStateHistory(taskId, oldState, newState, changedAt, reason));

        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUserId();
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskStateHistoryMapper, never()).toEntity(any(), any(), any(), any(), any());
        verify(taskStateHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Task State History By ID - Success as Admin")
    void getTaskStateHistoryById_SuccessAsAdmin() {
        when(taskStateHistoryRepository.findById(anyLong())).thenReturn(Optional.of(taskStateHistory));
        when(taskStateHistoryMapper.toResponse(any(TaskStateHistory.class))).thenReturn(taskStateHistoryResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        TaskStateHistoryResponse result = taskStateHistoryService.getTaskStateHistoryById(taskStateHistoryId);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponse, result);
        verify(taskStateHistoryRepository).findById(taskStateHistoryId);
        verify(taskStateHistoryMapper).toResponse(taskStateHistory);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State History By ID - Success for Project Manager in Same Department")
    void getTaskStateHistoryById_SuccessForProjectManager() {
        when(taskStateHistoryRepository.findById(anyLong())).thenReturn(Optional.of(taskStateHistory));
        when(taskStateHistoryMapper.toResponse(any(TaskStateHistory.class))).thenReturn(taskStateHistoryResponse);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        TaskStateHistoryResponse result = taskStateHistoryService.getTaskStateHistoryById(taskStateHistoryId);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponse, result);
        verify(taskStateHistoryRepository).findById(taskStateHistoryId);
        verify(taskStateHistoryMapper).toResponse(taskStateHistory);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State History By ID - Not Found")
    void getTaskStateHistoryById_NotFound() {
        when(taskStateHistoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskStateHistoryService.getTaskStateHistoryById(taskStateHistoryId));

        assertTrue(exception.getMessage().contains(taskStateHistoryId.toString()));
        verify(taskStateHistoryRepository).findById(taskStateHistoryId);
        verify(taskStateHistoryMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Get Task State History By ID - Unauthorized Access")
    void getTaskStateHistoryById_UnauthorizedAccess() {
        when(taskStateHistoryRepository.findById(anyLong())).thenReturn(Optional.of(taskStateHistory));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class,
                () -> taskStateHistoryService.getTaskStateHistoryById(taskStateHistoryId));

        verify(taskStateHistoryRepository).findById(taskStateHistoryId);
        verify(authService).getCurrentUser();
        verify(taskStateHistoryMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Get Task State Histories By Task ID - Success as Admin")
    void getTaskStateHistoriesByTaskId_SuccessAsAdmin() {
        when(taskRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(taskStateHistoryRepository.findByTaskIdWithDetails(any(UUID.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByTaskId(taskId);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        assertEquals(taskStateHistoryResponseList.size(), result.size());
        verify(taskRepository).existsByIdAndIsActiveTrue(taskId);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskStateHistoryRepository).findByTaskIdWithDetails(taskId);
        verify(taskStateHistoryMapper).toResponseList(taskStateHistoryList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By Task ID - Success as Project Manager in Same Department")
    void getTaskStateHistoriesByTaskId_SuccessAsProjectManager() {
        when(taskRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(taskStateHistoryRepository.findByTaskIdWithDetails(any(UUID.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByTaskId(taskId);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        assertEquals(taskStateHistoryResponseList.size(), result.size());
        verify(taskRepository).existsByIdAndIsActiveTrue(taskId);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskStateHistoryRepository).findByTaskIdWithDetails(taskId);
        verify(taskStateHistoryMapper).toResponseList(taskStateHistoryList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By Task ID - Task Not Found")
    void getTaskStateHistoriesByTaskId_TaskNotFound() {
        when(taskRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskStateHistoryService.getTaskStateHistoriesByTaskId(taskId));

        assertTrue(exception.getMessage().contains(taskId.toString()));
        verify(taskRepository).existsByIdAndIsActiveTrue(taskId);
        verify(taskStateHistoryRepository, never()).findByTaskIdWithDetails(any());
        verify(taskStateHistoryMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Get Task State Histories By Task ID - Unauthorized Access")
    void getTaskStateHistoriesByTaskId_UnauthorizedAccess() {
        when(taskRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class,
                () -> taskStateHistoryService.getTaskStateHistoriesByTaskId(taskId));

        verify(taskRepository).existsByIdAndIsActiveTrue(taskId);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUser();
        verify(taskStateHistoryRepository, never()).findByTaskIdWithDetails(any());
        verify(taskStateHistoryMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Get Task State Histories By Changed By User ID - Success as Admin")
    void getTaskStateHistoriesByChangedByUserId_SuccessAsAdmin() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(taskStateHistoryRepository.findByChangedByIdOrderByChangedAtDesc(any(UUID.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByChangedByUserId(userId);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        assertEquals(taskStateHistoryResponseList.size(), result.size());
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(taskStateHistoryRepository).findByChangedByIdOrderByChangedAtDesc(userId);
        verify(taskStateHistoryMapper).toResponseList(taskStateHistoryList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By Changed By User ID - Success as Project Manager with Department Filter")
    void getTaskStateHistoriesByChangedByUserId_SuccessWithDepartmentFilter() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(taskStateHistoryRepository.findByChangedByIdOrderByChangedAtDesc(any(UUID.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByChangedByUserId(userId);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(taskStateHistoryRepository).findByChangedByIdOrderByChangedAtDesc(userId);
        verify(taskStateHistoryMapper).toResponseList(anyList());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By Changed By User ID - User Not Found")
    void getTaskStateHistoriesByChangedByUserId_UserNotFound() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskStateHistoryService.getTaskStateHistoriesByChangedByUserId(userId));

        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(taskStateHistoryRepository, never()).findByChangedByIdOrderByChangedAtDesc(any());
        verify(taskStateHistoryMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Get Task State Histories By Old State - Success as Admin")
    void getTaskStateHistoriesByOldState_SuccessAsAdmin() {
        when(taskStateHistoryRepository.findByOldStateOrderByChangedAtDesc(any(TaskState.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByOldState(oldState);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        assertEquals(taskStateHistoryResponseList.size(), result.size());
        verify(taskStateHistoryRepository).findByOldStateOrderByChangedAtDesc(oldState);
        verify(taskStateHistoryMapper).toResponseList(taskStateHistoryList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By Old State - Success with Department Filter")
    void getTaskStateHistoriesByOldState_SuccessWithDepartmentFilter() {
        when(taskStateHistoryRepository.findByOldStateOrderByChangedAtDesc(any(TaskState.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByOldState(oldState);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        verify(taskStateHistoryRepository).findByOldStateOrderByChangedAtDesc(oldState);
        verify(taskStateHistoryMapper).toResponseList(anyList());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By New State - Success as Admin")
    void getTaskStateHistoriesByNewState_SuccessAsAdmin() {
        when(taskStateHistoryRepository.findByNewStateOrderByChangedAtDesc(any(TaskState.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByNewState(newState);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        assertEquals(taskStateHistoryResponseList.size(), result.size());
        verify(taskStateHistoryRepository).findByNewStateOrderByChangedAtDesc(newState);
        verify(taskStateHistoryMapper).toResponseList(taskStateHistoryList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By New State - Success with Department Filter")
    void getTaskStateHistoriesByNewState_SuccessWithDepartmentFilter() {
        when(taskStateHistoryRepository.findByNewStateOrderByChangedAtDesc(any(TaskState.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByNewState(newState);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        verify(taskStateHistoryRepository).findByNewStateOrderByChangedAtDesc(newState);
        verify(taskStateHistoryMapper).toResponseList(anyList());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By Changed At Between - Success as Admin")
    void getTaskStateHistoriesByChangedAtBetween_SuccessAsAdmin() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(taskStateHistoryRepository.findByChangedAtBetweenOrderByChangedAtDesc(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByChangedAtBetween(startDate, endDate);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        assertEquals(taskStateHistoryResponseList.size(), result.size());
        verify(taskStateHistoryRepository).findByChangedAtBetweenOrderByChangedAtDesc(startDate, endDate);
        verify(taskStateHistoryMapper).toResponseList(taskStateHistoryList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task State Histories By Changed At Between - Success with Department Filter")
    void getTaskStateHistoriesByChangedAtBetween_SuccessWithDepartmentFilter() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(taskStateHistoryRepository.findByChangedAtBetweenOrderByChangedAtDesc(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(taskStateHistoryList);
        when(taskStateHistoryMapper.toResponseList(anyList())).thenReturn(taskStateHistoryResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskStateHistoryResponse> result = taskStateHistoryService.getTaskStateHistoriesByChangedAtBetween(startDate, endDate);

        assertNotNull(result);
        assertEquals(taskStateHistoryResponseList, result);
        verify(taskStateHistoryRepository).findByChangedAtBetweenOrderByChangedAtDesc(startDate, endDate);
        verify(taskStateHistoryMapper).toResponseList(anyList());
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

    private Project createProject() {
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .title("Test Project")
                .description("Test Project Description")
                .department(department)
                .build();
        project.setIsActive(true);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }

    private Task createTask() {
        Task task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .userStory("As a user, I want to test")
                .acceptanceCriteria("It should work")
                .priority(TaskPriority.HIGH)
                .state(TaskState.BACKLOG)
                .project(project)
                .build();
        task.setIsActive(true);
        return task;
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
        return user;
    }

    private TaskStateHistory createTaskStateHistory() {
        TaskStateHistory taskStateHistory = TaskStateHistory.builder()
                .id(taskStateHistoryId)
                .oldState(oldState)
                .newState(newState)
                .reason(reason)
                .changedAt(changedAt)
                .changedBy(user)
                .task(task)
                .build();
        taskStateHistory.setIsActive(true);
        return taskStateHistory;
    }

    private TaskStateHistoryResponse createTaskStateHistoryResponse() {
        TaskStateHistoryResponse response = new TaskStateHistoryResponse();
        response.setId(taskStateHistoryId);
        response.setOldState(oldState);
        response.setNewState(newState);
        response.setReason(reason);
        response.setChangedAt(changedAt);

        UserResponse userResponse = createUserResponse("Test", "User", "test.user@example.com", null, departmentId);
        userResponse.setId(userId);

        response.setChangedBy(userResponse);
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

    private List<TaskStateHistory> createTaskStateHistoryList() {
        return Collections.singletonList(taskStateHistory);
    }

    private List<TaskStateHistoryResponse> createTaskStateHistoryResponseList() {
        return Collections.singletonList(taskStateHistoryResponse);
    }
} 