package com.patikadev.definex.advancedtaskmanager.repository;

import com.patikadev.definex.advancedtaskmanager.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId AND c.isActive = true")
    List<Comment> findAllByUserIdAndIsActiveTrue(UUID userId);

    boolean existsByIdAndIsActiveTrue(Long id);

    @Query("""
                SELECT c FROM Comment c
                JOIN FETCH c.task t
                WHERE t.id = :taskId AND c.isActive = true
                ORDER BY c.createdAt DESC
            """)
    List<Comment> findAllByTaskIdWithDetails(UUID taskId);
} 