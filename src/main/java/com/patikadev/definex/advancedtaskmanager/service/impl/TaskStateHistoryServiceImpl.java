package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.exception.UnauthorizedTaskAccessException;
import com.patikadev.definex.advancedtaskmanager.mapper.TaskStateHistoryMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.taskStateHistory.TaskStateHistoryResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.TaskStateHistory;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskStateHistoryRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.AuthService;
import com.patikadev.definex.advancedtaskmanager.service.TaskStateHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskStateHistoryServiceImpl implements TaskStateHistoryService {

    private final TaskStateHistoryRepository taskStateHistoryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final TaskStateHistoryMapper taskStateHistoryMapper;

    @Override
    @Transactional
    public void createTaskStateHistory(UUID taskId, TaskState oldState, TaskState newState, LocalDateTime changedAt, String reason) {
        Task task = findTaskById(taskId);
        UUID currentUserId = authService.getCurrentUserId();
        User currentUser = findUserById(currentUserId);

        TaskStateHistory taskStateHistory = taskStateHistoryMapper.toEntity(oldState, newState, reason, changedAt, currentUser);
        taskStateHistory.setTask(task);

        taskStateHistoryRepository.save(taskStateHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStateHistoryResponse getTaskStateHistoryById(Long id) {
        TaskStateHistory taskStateHistory = findTaskStateHistoryById(id);
        validateTaskHistoryViewPermission(taskStateHistory.getTask());

        return taskStateHistoryMapper.toResponse(taskStateHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStateHistoryResponse> getTaskStateHistoriesByTaskId(UUID taskId) {
        validateTaskExists(taskId);
        Task task = findTaskById(taskId);
        validateTaskHistoryViewPermission(task);
        List<TaskStateHistory> taskStateHistories = taskStateHistoryRepository.findByTaskIdWithDetails(taskId);

        return taskStateHistoryMapper.toResponseList(taskStateHistories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStateHistoryResponse> getTaskStateHistoriesByChangedByUserId(UUID userId) {
        validateUserExists(userId);
        UserResponse currentUser = authService.getCurrentUser();
        List<TaskStateHistory> taskStateHistories = taskStateHistoryRepository.findByChangedByIdOrderByChangedAtDesc(userId);

        return filterTaskHistoriesByDepartment(taskStateHistories, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStateHistoryResponse> getTaskStateHistoriesByOldState(TaskState oldState) {
        UserResponse currentUser = authService.getCurrentUser();
        List<TaskStateHistory> taskStateHistories = taskStateHistoryRepository.findByOldStateOrderByChangedAtDesc(oldState);

        return filterTaskHistoriesByDepartment(taskStateHistories, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStateHistoryResponse> getTaskStateHistoriesByNewState(TaskState newState) {
        UserResponse currentUser = authService.getCurrentUser();
        List<TaskStateHistory> taskStateHistories = taskStateHistoryRepository.findByNewStateOrderByChangedAtDesc(newState);

        return filterTaskHistoriesByDepartment(taskStateHistories, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStateHistoryResponse> getTaskStateHistoriesByChangedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        UserResponse currentUser = authService.getCurrentUser();
        List<TaskStateHistory> taskStateHistories = taskStateHistoryRepository.findByChangedAtBetweenOrderByChangedAtDesc(startDate, endDate);

        return filterTaskHistoriesByDepartment(taskStateHistories, currentUser);
    }

    private List<TaskStateHistoryResponse> filterTaskHistoriesByDepartment(List<TaskStateHistory> taskStateHistories, UserResponse currentUser) {
        if (isAdminOrProjectGroupManager(currentUser.getRoles())) {
            return taskStateHistoryMapper.toResponseList(taskStateHistories);
        } else {
            return taskStateHistoryMapper.toResponseList(
                    taskStateHistories.stream()
                            .filter(history -> Objects.equals(
                                    history.getTask().getProject().getDepartment().getId(),
                                    currentUser.getDepartmentId()))
                            .collect(Collectors.toList())
            );
        }
    }

    private Task findTaskById(UUID id) {
        return taskRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.TASK_NOT_FOUND.formatted(id)));
    }

    private User findUserById(UUID id) {
        return userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.USER_NOT_FOUND.formatted(id)));
    }

    private TaskStateHistory findTaskStateHistoryById(Long id) {
        return taskStateHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.TASK_STATE_HISTORY_NOT_FOUND.formatted(id)));
    }

    private void validateTaskExists(UUID taskId) {
        if (!taskRepository.existsByIdAndIsActiveTrue(taskId)) {
            throw new ResourceNotFoundException(
                    ErrorMessages.TASK_NOT_FOUND.formatted(taskId));
        }
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsByIdAndIsActiveTrue(userId)) {
            throw new ResourceNotFoundException(
                    ErrorMessages.USER_NOT_FOUND.formatted(userId));
        }
    }

    private void validateTaskHistoryViewPermission(Task task) {
        UserResponse currentUser = authService.getCurrentUser();
        Set<UserRole> roles = currentUser.getRoles();

        if (isAdminOrProjectGroupManager(roles)) {
            return;
        }

        if (currentUser.getDepartmentId() == null ||
                !Objects.equals(task.getProject().getDepartment().getId(), currentUser.getDepartmentId())) {
            throw new UnauthorizedTaskAccessException(ErrorMessages.UNAUTHORIZED_TASK_ACCESS);
        }
    }

    private boolean isAdminOrProjectGroupManager(Set<UserRole> roles) {
        return roles.contains(UserRole.ADMIN) || roles.contains(UserRole.PROJECT_GROUP_MANAGER);
    }
} 