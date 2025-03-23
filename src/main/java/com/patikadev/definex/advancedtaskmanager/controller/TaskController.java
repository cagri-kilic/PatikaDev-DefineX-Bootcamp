package com.patikadev.definex.advancedtaskmanager.controller;

import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.CreateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskStateRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskResponse;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        log.info("Request received to create a new task with title: {}", request.getTitle());
        TaskResponse response = taskService.createTask(request);
        log.info("Task created successfully with ID: {}", response.getId());
        return ResponseEntity.ok(ApiResponse.created(SuccessMessages.TASK_CREATED, response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        log.info("Request received to update task with ID: {}", id);
        TaskResponse response = taskService.updateTask(id, request);
        log.info("Task updated successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_UPDATED, response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<TaskDetailResponse>> getTaskById(@PathVariable UUID id) {
        log.info("Request received to get task details for ID: {}", id);
        TaskDetailResponse response = taskService.getTaskById(id);
        log.info("Task details retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASKS_FETCHED, response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks() {
        log.info("Request received to get all tasks");
        List<TaskResponse> responses = taskService.getAllTasks();
        log.info("Retrieved {} tasks successfully", responses.size());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASKS_FETCHED, responses));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByProjectId(@PathVariable UUID projectId) {
        log.info("Request received to get tasks for project ID: {}", projectId);
        List<TaskResponse> responses = taskService.getTasksByProjectId(projectId);
        log.info("Retrieved {} tasks for project ID: {}", responses.size(), projectId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASKS_FETCHED, responses));
    }

    @GetMapping("/assigned-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByAssignedUserId(@PathVariable UUID userId) {
        log.info("Request received to get tasks assigned to user ID: {}", userId);
        List<TaskResponse> responses = taskService.getTasksByAssignedUserId(userId);
        log.info("Retrieved {} tasks assigned to user ID: {}", responses.size(), userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASKS_FETCHED, responses));
    }

    @GetMapping("/state/{state}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByState(@PathVariable TaskState state) {
        log.info("Request received to get tasks by state: {}", state);
        List<TaskResponse> responses = taskService.getTasksByState(state);
        log.info("Retrieved {} tasks with state: {}", responses.size(), state);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASKS_FETCHED, responses));
    }

    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByPriority(@PathVariable TaskPriority priority) {
        log.info("Request received to get tasks by priority: {}", priority);
        List<TaskResponse> responses = taskService.getTasksByPriority(priority);
        log.info("Retrieved {} tasks with priority: {}", responses.size(), priority);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASKS_FETCHED, responses));
    }

    @PatchMapping("/{id}/state")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskState(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStateRequest request) {
        log.info("Request received to update task state for ID: {} to state: {}", id, request.getNewState());
        TaskResponse response = taskService.updateTaskState(id, request);
        log.info("Task state updated successfully for ID: {} to state: {}", id, request.getNewState());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_STATE_UPDATED, response));
    }

    @PostMapping("/{taskId}/assign/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTaskToUser(
            @PathVariable UUID taskId,
            @PathVariable UUID userId) {
        log.info("Request received to assign task ID: {} to user ID: {}", taskId, userId);
        TaskResponse response = taskService.assignTaskToUser(taskId, userId);
        log.info("Task ID: {} successfully assigned to user ID: {}", taskId, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_ASSIGNED, response));
    }

    @PostMapping("/{taskId}/unassign")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> unassignTask(@PathVariable UUID taskId) {
        log.info("Request received to unassign task ID: {}", taskId);
        TaskResponse response = taskService.unassignTask(taskId);
        log.info("Task ID: {} successfully unassigned", taskId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_UNASSIGNED, response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        log.info("Request received to delete task ID: {}", id);
        taskService.deleteTask(id);
        log.info("Task deleted successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_DELETED));
    }
} 