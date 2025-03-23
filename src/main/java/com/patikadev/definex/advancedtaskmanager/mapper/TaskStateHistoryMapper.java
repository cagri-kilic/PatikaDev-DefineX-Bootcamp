package com.patikadev.definex.advancedtaskmanager.mapper;

import com.patikadev.definex.advancedtaskmanager.model.dto.response.taskStateHistory.TaskStateHistoryResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.TaskStateHistory;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface TaskStateHistoryMapper {
    @Named("toResponse")
    @Mapping(target = "changedBy", qualifiedByName = "toResponse")
    TaskStateHistoryResponse toResponse(TaskStateHistory taskStateHistory);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    List<TaskStateHistoryResponse> toResponseList(List<TaskStateHistory> taskStateHistories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    TaskStateHistory toEntity(TaskState oldState, TaskState newState, String reason, LocalDateTime changedAt, User changedBy);

    @Named("toResponseSet")
    default Set<TaskStateHistoryResponse> toResponseSet(Set<TaskStateHistory> taskStateHistories) {
        if (taskStateHistories == null) {
            return null;
        }
        return taskStateHistories.stream()
                .filter(TaskStateHistory::getIsActive)
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
} 