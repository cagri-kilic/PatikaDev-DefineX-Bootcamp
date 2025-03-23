package com.patikadev.definex.advancedtaskmanager.repository;

import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByIdAndIsActiveTrue(UUID id);

    List<User> findAllByIsActiveTrue();

    List<User> findAllByDepartmentIdAndIsActiveTrue(Long departmentId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role AND u.isActive = true")
    List<User> findAllByRolesNameAndIsActiveTrue(@Param("role") UserRole role);

    boolean existsByEmail(String email);

    boolean existsByIdAndIsActiveTrue(UUID id);
} 