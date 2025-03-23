package com.patikadev.definex.advancedtaskmanager.controller;

import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.taskStateHistory.TaskStateHistoryResponse;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.service.TaskStateHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-state-histories")
@RequiredArgsConstructor
@Slf4j
public class TaskStateHistoryController {

    private final TaskStateHistoryService taskStateHistoryService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskStateHistoryResponse>> getTaskStateHistoryById(@PathVariable Long id) {
        log.info("Request received to get task state history by id: {}", id);
        TaskStateHistoryResponse response = taskStateHistoryService.getTaskStateHistoryById(id);
        log.info("Task state history retrieved successfully with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_STATE_HISTORY_FETCHED, response));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TaskStateHistoryResponse>>> getTaskStateHistoriesByTaskId(@PathVariable UUID taskId) {
        log.info("Request received to get task state histories by task id: {}", taskId);
        List<TaskStateHistoryResponse> responses = taskStateHistoryService.getTaskStateHistoriesByTaskId(taskId);
        log.info("Retrieved {} task state histories for task id: {}", responses.size(), taskId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_STATE_HISTORIES_FETCHED, responses));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TaskStateHistoryResponse>>> getTaskStateHistoriesByChangedByUserId(@PathVariable UUID userId) {
        log.info("Request received to get task state histories by user id: {}", userId);
        List<TaskStateHistoryResponse> responses = taskStateHistoryService.getTaskStateHistoriesByChangedByUserId(userId);
        log.info("Retrieved {} task state histories for user id: {}", responses.size(), userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_STATE_HISTORIES_FETCHED, responses));
    }

    @GetMapping("/old-state/{oldState}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TaskStateHistoryResponse>>> getTaskStateHistoriesByOldState(@PathVariable TaskState oldState) {
        log.info("Request received to get task state histories by old state: {}", oldState);
        List<TaskStateHistoryResponse> responses = taskStateHistoryService.getTaskStateHistoriesByOldState(oldState);
        log.info("Retrieved {} task state histories for old state: {}", responses.size(), oldState);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_STATE_HISTORIES_FETCHED, responses));
    }

    @GetMapping("/new-state/{newState}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TaskStateHistoryResponse>>> getTaskStateHistoriesByNewState(@PathVariable TaskState newState) {
        log.info("Request received to get task state histories by new state: {}", newState);
        List<TaskStateHistoryResponse> responses = taskStateHistoryService.getTaskStateHistoriesByNewState(newState);
        log.info("Retrieved {} task state histories for new state: {}", responses.size(), newState);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_STATE_HISTORIES_FETCHED, responses));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TaskStateHistoryResponse>>> getTaskStateHistoriesByChangedAtBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Request received to get task state histories between dates: {} and {}", startDate, endDate);
        List<TaskStateHistoryResponse> responses = taskStateHistoryService.getTaskStateHistoriesByChangedAtBetween(startDate, endDate);
        log.info("Retrieved {} task state histories between dates: {} and {}", responses.size(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TASK_STATE_HISTORIES_FETCHED, responses));
    }
} 