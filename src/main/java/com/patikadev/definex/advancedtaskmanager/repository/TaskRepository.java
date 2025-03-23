package com.patikadev.definex.advancedtaskmanager.repository;

import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findByIdAndIsActiveTrue(UUID id);

    List<Task> findAllByIsActiveTrue();

    List<Task> findAllByProjectIdAndIsActiveTrue(UUID projectId);

    List<Task> findAllByAssignedUserIdAndIsActiveTrue(UUID userId);

    List<Task> findAllByStateAndIsActiveTrue(TaskState state);

    List<Task> findAllByPriorityAndIsActiveTrue(TaskPriority priority);

    boolean existsByIdAndIsActiveTrue(UUID id);
} 