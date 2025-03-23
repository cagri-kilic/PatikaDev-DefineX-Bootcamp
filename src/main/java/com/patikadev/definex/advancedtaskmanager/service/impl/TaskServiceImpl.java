package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
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
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.ProjectRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.AuthService;
import com.patikadev.definex.advancedtaskmanager.service.TaskService;
import com.patikadev.definex.advancedtaskmanager.service.TaskStateHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskStateHistoryService taskStateHistoryService;
    private final TaskMapper taskMapper;
    private final AuthService authService;

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = findProjectById(request.getProjectId());
        validateTaskManagementPermission(project, ErrorMessages.UNAUTHORIZED_TASK_CREATE);
        User assignedUser = null;

        if (request.getAssignedUserId() != null) {
            assignedUser = findUserById(request.getAssignedUserId());
        }

        Task task = taskMapper.toEntity(request, project, assignedUser);
        Task savedTask = taskRepository.save(task);
        taskStateHistoryService.createTaskStateHistory(savedTask.getId(), null, TaskState.BACKLOG, LocalDateTime.now(), null);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request) {
        Task task = findTaskById(id);
        validateTaskManagementPermission(task.getProject(), ErrorMessages.UNAUTHORIZED_TASK_UPDATE);

        if (request.getAssignedUserId() != null) {
            User assignedUser = findUserById(request.getAssignedUserId());
            task.setAssignedUser(assignedUser);
        }

        taskMapper.updateEntityFromDto(request, task);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDetailResponse getTaskById(UUID id) {
        Task task = findTaskById(id);
        validateTaskViewPermission(task.getProject());

        return taskMapper.toDetailResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        UserResponse currentUser = authService.getCurrentUser();

        if (isAdminOrProjectGroupManager(currentUser.getRoles())) {
            List<Task> tasks = taskRepository.findAllByIsActiveTrue();
            return taskMapper.toResponseList(tasks);
        } else if (currentUser.getDepartmentId() != null) {
            List<Task> tasks = taskRepository.findAllByIsActiveTrue();
            return taskMapper.toResponseList(
                    tasks.stream()
                            .filter(task -> Objects.equals(task.getProject().getDepartment().getId(), currentUser.getDepartmentId()))
                            .toList()
            );
        }

        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProjectId(UUID projectId) {
        validateProjectExists(projectId);
        Project project = findProjectById(projectId);
        validateTaskViewPermission(project);
        List<Task> tasks = taskRepository.findAllByProjectIdAndIsActiveTrue(projectId);

        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssignedUserId(UUID userId) {
        validateUserExists(userId);
        UserResponse currentUser = authService.getCurrentUser();

        if (isAdminOrProjectGroupManager(currentUser.getRoles())) {
            List<Task> tasks = taskRepository.findAllByAssignedUserIdAndIsActiveTrue(userId);
            return taskMapper.toResponseList(tasks);
        } else {
            List<Task> tasks = taskRepository.findAllByAssignedUserIdAndIsActiveTrue(userId);
            return taskMapper.toResponseList(
                    tasks.stream()
                            .filter(task -> Objects.equals(task.getProject().getDepartment().getId(), currentUser.getDepartmentId()))
                            .toList()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByState(TaskState state) {
        UserResponse currentUser = authService.getCurrentUser();

        if (isAdminOrProjectGroupManager(currentUser.getRoles())) {
            List<Task> tasks = taskRepository.findAllByStateAndIsActiveTrue(state);
            return taskMapper.toResponseList(tasks);
        } else {
            List<Task> tasks = taskRepository.findAllByStateAndIsActiveTrue(state);
            return taskMapper.toResponseList(
                    tasks.stream()
                            .filter(task -> Objects.equals(task.getProject().getDepartment().getId(), currentUser.getDepartmentId()))
                            .toList()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByPriority(TaskPriority priority) {
        UserResponse currentUser = authService.getCurrentUser();

        if (isAdminOrProjectGroupManager(currentUser.getRoles())) {
            List<Task> tasks = taskRepository.findAllByPriorityAndIsActiveTrue(priority);
            return taskMapper.toResponseList(tasks);
        } else {
            List<Task> tasks = taskRepository.findAllByPriorityAndIsActiveTrue(priority);
            return taskMapper.toResponseList(
                    tasks.stream()
                            .filter(task -> Objects.equals(task.getProject().getDepartment().getId(), currentUser.getDepartmentId()))
                            .toList()
            );
        }
    }

    @Override
    @Transactional
    public TaskResponse updateTaskState(UUID id, UpdateTaskStateRequest request) {
        Task task = findTaskById(id);
        validateTaskStateUpdatePermission(task.getProject());
        TaskState currentState = task.getState();
        TaskState newState = request.getNewState();

        validateStateTransition(currentState, newState, request.getReason());
        task.setState(newState);
        task.setStateChangeReason(request.getReason());
        Task updatedTask = taskRepository.save(task);

        taskStateHistoryService.createTaskStateHistory(
                updatedTask.getId(),
                currentState,
                newState,
                LocalDateTime.now(),
                request.getReason());

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse assignTaskToUser(UUID taskId, UUID userId) {
        Task task = findTaskById(taskId);
        validateTaskManagementPermission(task.getProject(), ErrorMessages.UNAUTHORIZED_TASK_ASSIGNMENT);
        User user = findUserById(userId);
        task.setAssignedUser(user);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse unassignTask(UUID taskId) {
        Task task = findTaskById(taskId);
        validateTaskManagementPermission(task.getProject(), ErrorMessages.UNAUTHORIZED_TASK_ASSIGNMENT);
        task.setAssignedUser(null);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id) {
        Task task = findTaskById(id);
        validateTaskManagementPermission(task.getProject(), ErrorMessages.UNAUTHORIZED_TASK_DELETE);
        task.setIsActive(false);
        taskRepository.save(task);
    }

    private Task findTaskById(UUID id) {
        return taskRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.TASK_NOT_FOUND.formatted(id)));
    }

    private Project findProjectById(UUID id) {
        return projectRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.PROJECT_NOT_FOUND.formatted(id)));
    }

    private User findUserById(UUID id) {
        return userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.USER_NOT_FOUND.formatted(id)));
    }

    private void validateProjectExists(UUID projectId) {
        if (!projectRepository.existsByIdAndIsActiveTrue(projectId)) {
            throw new ResourceNotFoundException(
                    ErrorMessages.PROJECT_NOT_FOUND.formatted(projectId));
        }
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsByIdAndIsActiveTrue(userId)) {
            throw new ResourceNotFoundException(
                    ErrorMessages.USER_NOT_FOUND.formatted(userId));
        }
    }

    private void validateStateTransition(TaskState currentState, TaskState newState, String reason) {
        if (currentState == newState) {
            return;
        }

        validateReasonIfRequired(newState, reason);
        validateImmutableStates(currentState);
        validateTransitionPath(currentState, newState);
    }

    private void validateReasonIfRequired(TaskState newState, String reason) {
        boolean isReasonRequired = newState == TaskState.BLOCKED || newState == TaskState.CANCELLED;
        boolean isReasonMissing = reason == null || reason.isBlank();

        if (isReasonRequired && isReasonMissing) {
            throw new IllegalArgumentException(
                    ErrorMessages.REASON_REQUIRED.formatted(newState));
        }
    }

    private void validateImmutableStates(TaskState currentState) {
        if (currentState == TaskState.COMPLETED || currentState == TaskState.CANCELLED) {
            throw new InvalidTaskStateTransitionException(
                    ErrorMessages.TASK_STATE_CANNOT_BE_CHANGED.formatted(currentState));
        }
    }

    private void validateTransitionPath(TaskState currentState, TaskState newState) {
        switch (currentState) {
            case BACKLOG -> {
                if (newState != TaskState.IN_ANALYSIS && newState != TaskState.CANCELLED) {
                    throwInvalidTransitionException(currentState, newState);
                }
            }
            case IN_ANALYSIS -> {
                if (newState != TaskState.BACKLOG && newState != TaskState.IN_PROGRESS
                        && newState != TaskState.BLOCKED && newState != TaskState.CANCELLED) {
                    throwInvalidTransitionException(currentState, newState);
                }
            }
            case IN_PROGRESS -> {
                if (newState != TaskState.IN_ANALYSIS && newState != TaskState.COMPLETED
                        && newState != TaskState.BLOCKED && newState != TaskState.CANCELLED) {
                    throwInvalidTransitionException(currentState, newState);
                }
            }
            case BLOCKED -> {
                if (newState != TaskState.IN_ANALYSIS && newState != TaskState.IN_PROGRESS
                        && newState != TaskState.CANCELLED) {
                    throwInvalidTransitionException(currentState, newState);
                }
            }
            default -> throwInvalidTransitionException(currentState, newState);
        }
    }

    private void throwInvalidTransitionException(TaskState currentState, TaskState newState) {
        throw new InvalidTaskStateTransitionException(
                ErrorMessages.INVALID_STATE_TRANSITION.formatted(currentState, newState));
    }

    private void validateTaskManagementPermission(Project project, String errorMessage) {
        UserResponse currentUser = authService.getCurrentUser();
        Set<UserRole> roles = currentUser.getRoles();

        if (isAdminOrProjectGroupManager(roles)) {
            return;
        }

        if ((roles.contains(UserRole.PROJECT_MANAGER) || roles.contains(UserRole.TEAM_LEADER))
                && (currentUser.getDepartmentId() == null ||
                !Objects.equals(project.getDepartment().getId(), currentUser.getDepartmentId()))) {
            throw new UnauthorizedTaskAccessException(errorMessage);
        }

        if (roles.contains(UserRole.TEAM_MEMBER) &&
                !roles.contains(UserRole.PROJECT_MANAGER) &&
                !roles.contains(UserRole.TEAM_LEADER)) {
            throw new UnauthorizedTaskAccessException(errorMessage);
        }
    }

    private void validateTaskViewPermission(Project project) {
        UserResponse currentUser = authService.getCurrentUser();
        Set<UserRole> roles = currentUser.getRoles();

        if (isAdminOrProjectGroupManager(roles)) {
            return;
        }

        if (currentUser.getDepartmentId() == null ||
                !Objects.equals(project.getDepartment().getId(), currentUser.getDepartmentId())) {
            throw new UnauthorizedTaskAccessException(ErrorMessages.UNAUTHORIZED_TASK_ACCESS);
        }
    }

    private void validateTaskStateUpdatePermission(Project project) {
        UserResponse currentUser = authService.getCurrentUser();
        Set<UserRole> roles = currentUser.getRoles();

        if (isAdminOrProjectGroupManager(roles)) {
            return;
        }

        if (currentUser.getDepartmentId() == null ||
                !Objects.equals(project.getDepartment().getId(), currentUser.getDepartmentId())) {
            throw new UnauthorizedTaskAccessException(ErrorMessages.TEAM_MEMBER_RESTRICTED_ACCESS);
        }
    }

    private boolean isAdminOrProjectGroupManager(Set<UserRole> roles) {
        return roles.contains(UserRole.ADMIN) || roles.contains(UserRole.PROJECT_GROUP_MANAGER);
    }
} 