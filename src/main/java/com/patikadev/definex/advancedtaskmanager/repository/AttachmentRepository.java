package com.patikadev.definex.advancedtaskmanager.repository;

import com.patikadev.definex.advancedtaskmanager.model.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Optional<Attachment> findByIdAndIsActiveTrue(Long id);

    List<Attachment> findAllByUploadedByUserIdAndIsActiveTrue(UUID userId);

    boolean existsByIdAndIsActiveTrue(Long id);

    @Query("""
                SELECT a FROM Attachment a
                JOIN FETCH a.task t
                JOIN FETCH a.uploadedByUser u
                WHERE t.id = :taskId AND a.isActive = true
                ORDER BY a.createdAt DESC
            """)
    List<Attachment> findAllByTaskIdWithDetails(UUID taskId);
} 