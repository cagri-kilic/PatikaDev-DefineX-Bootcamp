package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.exception.InvalidTaskStateTransitionException;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.exception.UnauthorizedTaskAccessException;
import com.patikadev.definex.advancedtaskmanager.mapper.TaskMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.CreateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskStateRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.ProjectRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.impl.TaskServiceImpl;
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
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskStateHistoryService taskStateHistoryService;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task;
    private Project project;
    private Department department;
    private User user;
    private CreateTaskRequest createTaskRequest;
    private UpdateTaskRequest updateTaskRequest;
    private TaskResponse taskResponse;
    private TaskDetailResponse taskDetailResponse;
    private List<Task> taskList;
    private List<TaskResponse> taskResponseList;
    private final UUID taskId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final Long departmentId = 1L;
    private UserResponse adminUserResponse;
    private UserResponse projectManagerUserResponse;
    private UserResponse teamMemberUserResponse;
    private UserResponse userWithOtherDepartment;

    @BeforeEach
    void setUp() {
        department = createDepartment();
        project = createProject();
        user = createUser();
        task = createTask();
        createTaskRequest = createCreateTaskRequest();
        updateTaskRequest = createUpdateTaskRequest();
        taskResponse = createTaskResponse();
        taskDetailResponse = createTaskDetailResponse();
        taskList = createTaskList();
        taskResponseList = createTaskResponseList();

        adminUserResponse = createUserResponse("Admin", "User", "admin@example.com",
                new HashSet<>(Collections.singletonList(UserRole.ADMIN)), departmentId);
        projectManagerUserResponse = createUserResponse("PM", "User", "pm@example.com",
                new HashSet<>(Collections.singletonList(UserRole.PROJECT_MANAGER)), departmentId);
        UserResponse teamLeaderUserResponse = createUserResponse("TL", "User", "tl@example.com",
                new HashSet<>(Collections.singletonList(UserRole.TEAM_LEADER)), departmentId);
        teamMemberUserResponse = createUserResponse("TM", "User", "tm@example.com",
                new HashSet<>(Collections.singletonList(UserRole.TEAM_MEMBER)), departmentId);
        userWithOtherDepartment = createUserResponse("Other", "User", "other@example.com",
                new HashSet<>(Collections.singletonList(UserRole.PROJECT_MANAGER)), departmentId + 1);
    }

    @Test
    @DisplayName("Create Task - Success with Admin Role")
    void createTask_SuccessWithAdminRole() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(taskMapper.toEntity(any(CreateTaskRequest.class), any(Project.class), any(User.class))).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);
        doNothing().when(taskStateHistoryService).createTaskStateHistory(any(UUID.class), any(), any(TaskState.class), any(LocalDateTime.class), any());

        TaskResponse result = taskService.createTask(createTaskRequest);

        assertNotNull(result);
        assertEquals(taskResponse, result);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskMapper).toEntity(eq(createTaskRequest), eq(project), eq(user));
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(task);
        verify(authService).getCurrentUser();
        verify(taskStateHistoryService).createTaskStateHistory(eq(taskId), isNull(), eq(TaskState.BACKLOG), any(LocalDateTime.class), isNull());
    }

    @Test
    @DisplayName("Create Task - Success with Project Manager Role")
    void createTask_SuccessWithProjectManagerRole() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(taskMapper.toEntity(any(CreateTaskRequest.class), any(Project.class), any(User.class))).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);
        doNothing().when(taskStateHistoryService).createTaskStateHistory(any(UUID.class), any(), any(TaskState.class), any(LocalDateTime.class), any());

        TaskResponse result = taskService.createTask(createTaskRequest);

        assertNotNull(result);
        assertEquals(taskResponse, result);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskMapper).toEntity(eq(createTaskRequest), eq(project), eq(user));
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(task);
        verify(authService).getCurrentUser();
        verify(taskStateHistoryService).createTaskStateHistory(eq(taskId), isNull(), eq(TaskState.BACKLOG), any(LocalDateTime.class), isNull());
    }

    @Test
    @DisplayName("Create Task - Unauthorized Access with Team Member Role")
    void createTask_UnauthorizedAccessWithTeamMemberRole() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(teamMemberUserResponse);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.createTask(createTaskRequest));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(authService).getCurrentUser();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(taskMapper, never()).toEntity(any(), any(), any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Task - Unauthorized Access with Different Department")
    void createTask_UnauthorizedAccessWithDifferentDepartment() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.createTask(createTaskRequest));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(authService).getCurrentUser();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(taskMapper, never()).toEntity(any(), any(), any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Task - Project Not Found")
    void createTask_ProjectNotFound() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(createTaskRequest));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(taskMapper, never()).toEntity(any(), any(), any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Task - User Not Found")
    void createTask_UserNotFound() {
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(createTaskRequest));
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskMapper, never()).toEntity(any(), any(), any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Task - Success")
    void updateTask_Success() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        TaskResponse result = taskService.updateTask(taskId, updateTaskRequest);

        assertNotNull(result);
        assertEquals(taskResponse, result);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskMapper).updateEntityFromDto(updateTaskRequest, task);
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(task);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Update Task - Unauthorized Access")
    void updateTask_UnauthorizedAccess() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.updateTask(taskId, updateTaskRequest));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUser();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(taskMapper, never()).updateEntityFromDto(any(), any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Task - User Not Found")
    void updateTask_UserNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(taskId, updateTaskRequest));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskMapper, never()).updateEntityFromDto(any(), any());
        verify(taskRepository, never()).save(any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task By Id - Success")
    void getTaskById_Success() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(taskMapper.toDetailResponse(any(Task.class))).thenReturn(taskDetailResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        TaskDetailResponse result = taskService.getTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskDetailResponse, result);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskMapper).toDetailResponse(task);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Task By Id - Unauthorized Access")
    void getTaskById_UnauthorizedAccess() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.getTaskById(taskId));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUser();
        verify(taskMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Get All Tasks - Success as Admin")
    void getAllTasks_SuccessAsAdmin() {
        when(taskRepository.findAllByIsActiveTrue()).thenReturn(taskList);
        when(taskMapper.toResponseList(anyList())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskResponse> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        assertEquals(taskResponseList.size(), result.size());
        verify(taskRepository).findAllByIsActiveTrue();
        verify(taskMapper).toResponseList(taskList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Tasks By Project Id - Success")
    void getTasksByProjectId_Success() {
        when(projectRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(taskRepository.findAllByProjectIdAndIsActiveTrue(any(UUID.class))).thenReturn(taskList);
        when(taskMapper.toResponseList(anyList())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskResponse> result = taskService.getTasksByProjectId(projectId);

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        assertEquals(taskResponseList.size(), result.size());
        verify(projectRepository).existsByIdAndIsActiveTrue(projectId);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(taskRepository).findAllByProjectIdAndIsActiveTrue(projectId);
        verify(taskMapper).toResponseList(taskList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Tasks By Project Id - Project Not Found")
    void getTasksByProjectId_ProjectNotFound() {
        when(projectRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByProjectId(projectId));
        verify(projectRepository).existsByIdAndIsActiveTrue(projectId);
        verify(taskRepository, never()).findAllByProjectIdAndIsActiveTrue(any());
        verify(taskMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Get Tasks By Assigned User Id - Success")
    void getTasksByAssignedUserId_Success() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(taskRepository.findAllByAssignedUserIdAndIsActiveTrue(any(UUID.class))).thenReturn(taskList);
        when(taskMapper.toResponseList(anyList())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskResponse> result = taskService.getTasksByAssignedUserId(userId);

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        assertEquals(taskResponseList.size(), result.size());
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(taskRepository).findAllByAssignedUserIdAndIsActiveTrue(userId);
        verify(taskMapper).toResponseList(taskList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Tasks By Assigned User Id - User Not Found")
    void getTasksByAssignedUserId_UserNotFound() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByAssignedUserId(userId));
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(taskRepository, never()).findAllByAssignedUserIdAndIsActiveTrue(any());
        verify(taskMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Get Tasks By State - Success")
    void getTasksByState_Success() {
        when(taskRepository.findAllByStateAndIsActiveTrue(any(TaskState.class))).thenReturn(taskList);
        when(taskMapper.toResponseList(anyList())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskResponse> result = taskService.getTasksByState(TaskState.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        assertEquals(taskResponseList.size(), result.size());
        verify(taskRepository).findAllByStateAndIsActiveTrue(TaskState.IN_PROGRESS);
        verify(taskMapper).toResponseList(taskList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Tasks By Priority - Success")
    void getTasksByPriority_Success() {
        when(taskRepository.findAllByPriorityAndIsActiveTrue(any(TaskPriority.class))).thenReturn(taskList);
        when(taskMapper.toResponseList(anyList())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        List<TaskResponse> result = taskService.getTasksByPriority(TaskPriority.HIGH);

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        assertEquals(taskResponseList.size(), result.size());
        verify(taskRepository).findAllByPriorityAndIsActiveTrue(TaskPriority.HIGH);
        verify(taskMapper).toResponseList(taskList);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Update Task State - Success")
    void updateTaskState_Success() {
        Task taskWithState = createTask();
        taskWithState.setState(TaskState.IN_ANALYSIS);

        UpdateTaskStateRequest stateRequest = new UpdateTaskStateRequest();
        stateRequest.setNewState(TaskState.IN_PROGRESS);
        stateRequest.setReason("Moving to development");

        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(taskWithState));
        when(taskRepository.save(any(Task.class))).thenReturn(taskWithState);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);
        doNothing().when(taskStateHistoryService).createTaskStateHistory(any(UUID.class), any(TaskState.class), any(TaskState.class), any(LocalDateTime.class), anyString());

        TaskResponse result = taskService.updateTaskState(taskId, stateRequest);

        assertNotNull(result);
        assertEquals(taskResponse, result);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskRepository).save(taskWithState);
        verify(taskStateHistoryService).createTaskStateHistory(eq(taskId), eq(TaskState.IN_ANALYSIS), eq(TaskState.IN_PROGRESS), any(LocalDateTime.class), eq("Moving to development"));
        verify(taskMapper).toResponse(taskWithState);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Update Task State - Invalid Transition")
    void updateTaskState_InvalidTransition() {
        Task taskWithState = createTask();
        taskWithState.setState(TaskState.BACKLOG);

        UpdateTaskStateRequest stateRequest = new UpdateTaskStateRequest();
        stateRequest.setNewState(TaskState.COMPLETED);

        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(taskWithState));
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        assertThrows(InvalidTaskStateTransitionException.class, () -> taskService.updateTaskState(taskId, stateRequest));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskRepository, never()).save(any());
        verify(taskStateHistoryService, never()).createTaskStateHistory(any(), any(), any(), any(), any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Update Task State - No Reason for Required States")
    void updateTaskState_NoReasonForRequiredStates() {
        Task taskWithState = createTask();
        taskWithState.setState(TaskState.IN_PROGRESS);

        UpdateTaskStateRequest stateRequest = new UpdateTaskStateRequest();
        stateRequest.setNewState(TaskState.BLOCKED);

        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(taskWithState));
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTaskState(taskId, stateRequest));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskRepository, never()).save(any());
        verify(taskStateHistoryService, never()).createTaskStateHistory(any(), any(), any(), any(), any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Update Task State - Unauthorized Access")
    void updateTaskState_UnauthorizedAccess() {
        Task taskWithState = createTask();
        taskWithState.setState(TaskState.IN_ANALYSIS);

        UpdateTaskStateRequest stateRequest = new UpdateTaskStateRequest();
        stateRequest.setNewState(TaskState.IN_PROGRESS);
        stateRequest.setReason("Moving to development");

        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(taskWithState));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.updateTaskState(taskId, stateRequest));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUser();
        verify(taskRepository, never()).save(any());
        verify(taskStateHistoryService, never()).createTaskStateHistory(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Assign Task To User - Success")
    void assignTaskToUser_Success() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        TaskResponse result = taskService.assignTaskToUser(taskId, userId);

        assertNotNull(result);
        assertEquals(taskResponse, result);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(task);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Assign Task To User - Unauthorized Access")
    void assignTaskToUser_UnauthorizedAccess() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.assignTaskToUser(taskId, userId));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUser();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete Task - Success")
    void deleteTask_Success() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        taskService.deleteTask(taskId);

        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskRepository).save(task);
        verify(authService).getCurrentUser();
        assertFalse(task.getIsActive());
    }

    @Test
    @DisplayName("Delete Task - Unauthorized Access")
    void deleteTask_UnauthorizedAccess() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.deleteTask(taskId));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUser();
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get All Tasks - Success with Department Filter for Project Manager")
    void getAllTasks_SuccessWithDepartmentFilter() {
        when(taskRepository.findAllByIsActiveTrue()).thenReturn(taskList);
        when(taskMapper.toResponseList(any())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskResponse> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        verify(taskRepository).findAllByIsActiveTrue();
        verify(taskMapper).toResponseList(any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Tasks By Assigned User Id - Success with Department Filter")
    void getTasksByAssignedUserId_SuccessWithDepartmentFilter() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(taskRepository.findAllByAssignedUserIdAndIsActiveTrue(any(UUID.class))).thenReturn(taskList);
        when(taskMapper.toResponseList(any())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskResponse> result = taskService.getTasksByAssignedUserId(userId);

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(taskRepository).findAllByAssignedUserIdAndIsActiveTrue(userId);
        verify(taskMapper).toResponseList(any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Tasks By State - Success with Department Filter")
    void getTasksByState_SuccessWithDepartmentFilter() {
        when(taskRepository.findAllByStateAndIsActiveTrue(any(TaskState.class))).thenReturn(taskList);
        when(taskMapper.toResponseList(any())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskResponse> result = taskService.getTasksByState(TaskState.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        verify(taskRepository).findAllByStateAndIsActiveTrue(TaskState.IN_PROGRESS);
        verify(taskMapper).toResponseList(any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Get Tasks By Priority - Success with Department Filter")
    void getTasksByPriority_SuccessWithDepartmentFilter() {
        when(taskRepository.findAllByPriorityAndIsActiveTrue(any(TaskPriority.class))).thenReturn(taskList);
        when(taskMapper.toResponseList(any())).thenReturn(taskResponseList);
        when(authService.getCurrentUser()).thenReturn(projectManagerUserResponse);

        List<TaskResponse> result = taskService.getTasksByPriority(TaskPriority.HIGH);

        assertNotNull(result);
        assertEquals(taskResponseList, result);
        verify(taskRepository).findAllByPriorityAndIsActiveTrue(TaskPriority.HIGH);
        verify(taskMapper).toResponseList(any());
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Update Task State - Immutable States")
    void updateTaskState_ImmutableStates() {
        Task taskWithState = createTask();
        taskWithState.setState(TaskState.COMPLETED);

        UpdateTaskStateRequest stateRequest = new UpdateTaskStateRequest();
        stateRequest.setNewState(TaskState.IN_PROGRESS);
        stateRequest.setReason("Reopening task");

        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(taskWithState));
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        assertThrows(InvalidTaskStateTransitionException.class, () -> taskService.updateTaskState(taskId, stateRequest));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUser();
        verify(taskRepository, never()).save(any());
        verify(taskStateHistoryService, never()).createTaskStateHistory(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Create Task - Null Assigned User")
    void createTask_NullAssignedUser() {
        CreateTaskRequest requestWithoutUser = createCreateTaskRequest();
        requestWithoutUser.setAssignedUserId(null);

        Task taskWithoutUser = createTask();
        taskWithoutUser.setAssignedUser(null);

        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(taskMapper.toEntity(any(CreateTaskRequest.class), any(Project.class), isNull())).thenReturn(taskWithoutUser);
        when(taskRepository.save(any(Task.class))).thenReturn(taskWithoutUser);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);
        doNothing().when(taskStateHistoryService).createTaskStateHistory(any(UUID.class), any(), any(TaskState.class), any(LocalDateTime.class), any());

        TaskResponse result = taskService.createTask(requestWithoutUser);

        assertNotNull(result);
        assertEquals(taskResponse, result);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(taskMapper).toEntity(eq(requestWithoutUser), eq(project), isNull());
        verify(taskRepository).save(taskWithoutUser);
        verify(taskMapper).toResponse(taskWithoutUser);
        verify(authService).getCurrentUser();
        verify(taskStateHistoryService).createTaskStateHistory(eq(taskId), isNull(), eq(TaskState.BACKLOG), any(LocalDateTime.class), isNull());
    }

    @Test
    @DisplayName("Update Task State - Same State")
    void updateTaskState_SameState() {
        Task taskWithState = createTask();
        taskWithState.setState(TaskState.IN_ANALYSIS);

        UpdateTaskStateRequest stateRequest = new UpdateTaskStateRequest();
        stateRequest.setNewState(TaskState.IN_ANALYSIS);

        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(taskWithState));
        when(taskRepository.save(any(Task.class))).thenReturn(taskWithState);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);
        doNothing().when(taskStateHistoryService).createTaskStateHistory(any(UUID.class), any(TaskState.class), any(TaskState.class), any(LocalDateTime.class), any());

        TaskResponse result = taskService.updateTaskState(taskId, stateRequest);

        assertNotNull(result);
        assertEquals(taskResponse, result);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskRepository).save(taskWithState);
        verify(taskMapper).toResponse(taskWithState);
        verify(authService).getCurrentUser();
        verify(taskStateHistoryService).createTaskStateHistory(eq(taskId), eq(TaskState.IN_ANALYSIS), eq(TaskState.IN_ANALYSIS), any(LocalDateTime.class), isNull());
    }

    @Test
    @DisplayName("Get Tasks By Project Id - Unauthorized Access")
    void getTasksByProjectId_UnauthorizedAccess() {
        when(projectRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(projectRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.getTasksByProjectId(projectId));
        verify(projectRepository).existsByIdAndIsActiveTrue(projectId);
        verify(projectRepository).findByIdAndIsActiveTrue(projectId);
        verify(authService).getCurrentUser();
        verify(taskRepository, never()).findAllByProjectIdAndIsActiveTrue(any());
        verify(taskMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Unassign Task - Success")
    void unassignTask_Success() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(authService.getCurrentUser()).thenReturn(adminUserResponse);

        TaskResponse result = taskService.unassignTask(taskId);

        assertNotNull(result);
        assertEquals(taskResponse, result);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(task);
        verify(authService).getCurrentUser();
    }

    @Test
    @DisplayName("Unassign Task - Unauthorized Access")
    void unassignTask_UnauthorizedAccess() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUser()).thenReturn(userWithOtherDepartment);

        assertThrows(UnauthorizedTaskAccessException.class, () -> taskService.unassignTask(taskId));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService).getCurrentUser();
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Task By Id - Task Not Found")
    void getTaskById_TaskNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(taskId));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(taskMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Update Task - Task Not Found")
    void updateTask_TaskNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(taskId, updateTaskRequest));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(taskMapper, never()).updateEntityFromDto(any(), any());
        verify(taskRepository, never()).save(any());
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
                .id(projectId)
                .title("Test Project")
                .description("Test Project Description")
                .department(department)
                .build();
        project.setIsActive(true);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }

    private User createUser() {
        User user = User.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("Password123!")
                .build();
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private Task createTask() {
        Task task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .userStory("As a user, I want to test the task service")
                .acceptanceCriteria("Task service tests are passing")
                .state(TaskState.BACKLOG)
                .priority(TaskPriority.HIGH)
                .project(project)
                .assignedUser(user)
                .comments(new HashSet<>())
                .attachments(new HashSet<>())
                .stateHistories(new HashSet<>())
                .build();
        task.setIsActive(true);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private CreateTaskRequest createCreateTaskRequest() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Test Task");
        request.setUserStory("As a user, I want to test the task service");
        request.setAcceptanceCriteria("Task service tests are passing");
        request.setPriority(TaskPriority.HIGH);
        request.setProjectId(projectId);
        request.setAssignedUserId(userId);
        return request;
    }

    private UpdateTaskRequest createUpdateTaskRequest() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setUserStory("As a user, I want to update the task");
        request.setAcceptanceCriteria("Task update tests are passing");
        request.setPriority(TaskPriority.MEDIUM);
        request.setAssignedUserId(userId);
        return request;
    }

    private TaskResponse createTaskResponse() {
        TaskResponse response = new TaskResponse();
        response.setId(taskId);
        response.setTitle("Test Task");
        response.setUserStory("As a user, I want to test the task service");
        response.setAcceptanceCriteria("Task service tests are passing");
        response.setState(TaskState.BACKLOG);
        response.setPriority(TaskPriority.HIGH);
        response.setProjectId(projectId);
        response.setProjectTitle("Test Project");
        response.setAssignedUserId(userId);
        response.setAssignedUserName("Test User");
        response.setActive(true);
        response.setTotalComments(0);
        response.setTotalAttachments(0);
        return response;
    }

    private TaskDetailResponse createTaskDetailResponse() {
        TaskDetailResponse response = new TaskDetailResponse();
        response.setId(taskId);
        response.setTitle("Test Task");
        response.setUserStory("As a user, I want to test the task service");
        response.setAcceptanceCriteria("Task service tests are passing");
        response.setState(TaskState.BACKLOG);
        response.setPriority(TaskPriority.HIGH);
        response.setProjectId(projectId);
        response.setProjectTitle("Test Project");
        response.setAssignedUserId(userId);
        response.setAssignedUserName("Test User");
        response.setActive(true);
        response.setTotalComments(0);
        response.setTotalAttachments(0);
        response.setComments(new HashSet<>());
        response.setAttachments(new HashSet<>());
        response.setStateHistories(new HashSet<>());
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

    private List<Task> createTaskList() {
        return Collections.singletonList(task);
    }

    private List<TaskResponse> createTaskResponseList() {
        return Collections.singletonList(taskResponse);
    }
} 