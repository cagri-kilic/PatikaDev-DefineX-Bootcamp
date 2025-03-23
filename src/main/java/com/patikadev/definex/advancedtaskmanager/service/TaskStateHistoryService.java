package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.dto.response.taskStateHistory.TaskStateHistoryResponse;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskStateHistoryService {

    void createTaskStateHistory(UUID taskId, TaskState oldState, TaskState newState, LocalDateTime changedAt, String reason);

    TaskStateHistoryResponse getTaskStateHistoryById(Long id);

    List<TaskStateHistoryResponse> getTaskStateHistoriesByTaskId(UUID taskId);

    List<TaskStateHistoryResponse> getTaskStateHistoriesByChangedByUserId(UUID userId);

    List<TaskStateHistoryResponse> getTaskStateHistoriesByOldState(TaskState oldState);

    List<TaskStateHistoryResponse> getTaskStateHistoriesByNewState(TaskState newState);

    List<TaskStateHistoryResponse> getTaskStateHistoriesByChangedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
} 