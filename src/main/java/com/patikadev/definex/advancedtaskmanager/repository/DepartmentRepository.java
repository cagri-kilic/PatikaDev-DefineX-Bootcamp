package com.patikadev.definex.advancedtaskmanager.repository;

import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByIdAndIsActiveTrue(Long id);

    Optional<Department> findByNameAndIsActiveTrue(String name);

    List<Department> findAllByIsActiveTrue();

    boolean existsByIdAndIsActiveTrue(Long id);
} 