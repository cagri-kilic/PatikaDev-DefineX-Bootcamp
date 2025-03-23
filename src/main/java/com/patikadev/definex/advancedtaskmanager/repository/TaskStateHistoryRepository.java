package com.patikadev.definex.advancedtaskmanager.repository;

import com.patikadev.definex.advancedtaskmanager.model.entity.TaskStateHistory;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskStateHistoryRepository extends JpaRepository<TaskStateHistory, Long> {

    @Query("""
                SELECT tsh FROM TaskStateHistory tsh
                JOIN FETCH tsh.task t
                JOIN FETCH tsh.changedBy u
                WHERE t.id = :taskId
                ORDER BY tsh.changedAt DESC
            """)
    List<TaskStateHistory> findByTaskIdWithDetails(UUID taskId);

    List<TaskStateHistory> findByChangedByIdOrderByChangedAtDesc(UUID userId);

    List<TaskStateHistory> findByOldStateOrderByChangedAtDesc(TaskState oldState);

    List<TaskStateHistory> findByNewStateOrderByChangedAtDesc(TaskState newState);

    List<TaskStateHistory> findByChangedAtBetweenOrderByChangedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsById(Long id);
} 