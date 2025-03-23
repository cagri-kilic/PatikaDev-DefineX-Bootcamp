package com.patikadev.definex.advancedtaskmanager.repository;

import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByIdAndIsActiveTrue(UUID id);

    List<Project> findAllByIsActiveTrue();

    List<Project> findAllByDepartmentIdAndIsActiveTrue(Long departmentId);

    List<Project> findAllByStatusAndIsActiveTrue(ProjectStatus status);

    @Query("SELECT p FROM Project p JOIN p.teamMembers m WHERE m.id = :userId AND p.isActive = true")
    List<Project> findAllByTeamMemberIdAndIsActiveTrue(@Param("userId") UUID userId);

    boolean existsByIdAndIsActiveTrue(UUID id);
} 