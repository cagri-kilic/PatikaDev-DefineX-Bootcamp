package com.patikadev.definex.advancedtaskmanager.model.dto.response.taskStateHistory;

import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskStateHistoryResponse {
    Long id;
    TaskState oldState;
    TaskState newState;
    String reason;
    LocalDateTime changedAt;
    UserResponse changedBy;
}