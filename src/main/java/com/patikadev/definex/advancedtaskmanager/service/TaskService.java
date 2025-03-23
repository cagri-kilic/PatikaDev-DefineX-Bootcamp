package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.CreateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskStateRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskResponse;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse updateTask(UUID id, UpdateTaskRequest request);

    TaskDetailResponse getTaskById(UUID id);

    List<TaskResponse> getAllTasks();

    List<TaskResponse> getTasksByProjectId(UUID projectId);

    List<TaskResponse> getTasksByAssignedUserId(UUID userId);

    List<TaskResponse> getTasksByState(TaskState state);

    List<TaskResponse> getTasksByPriority(TaskPriority priority);

    TaskResponse updateTaskState(UUID id, UpdateTaskStateRequest request);

    TaskResponse assignTaskToUser(UUID taskId, UUID userId);

    TaskResponse unassignTask(UUID taskId);

    void deleteTask(UUID id);
} 