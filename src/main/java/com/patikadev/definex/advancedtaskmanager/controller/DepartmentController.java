package com.patikadev.definex.advancedtaskmanager.controller;

import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.CreateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.UpdateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentDetailResponse;
import com.patikadev.definex.advancedtaskmanager.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        log.info("Request received to create a new department with name: {}", request.getName());
        DepartmentResponse departmentResponse = departmentService.createDepartment(request);
        log.info("Department created successfully with ID: {}", departmentResponse.getId());
        return ResponseEntity.ok(ApiResponse.created(SuccessMessages.DEPARTMENT_CREATED, departmentResponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        log.info("Request received to update department with ID: {}", id);
        DepartmentResponse departmentResponse = departmentService.updateDepartment(id, request);
        log.info("Department updated successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.DEPARTMENT_UPDATED, departmentResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<DepartmentDetailResponse>> getDepartmentById(@PathVariable Long id) {
        log.info("Request received to get department details for ID: {}", id);
        DepartmentDetailResponse departmentResponse = departmentService.getDepartmentById(id);
        log.info("Department details retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.DEPARTMENTS_FETCHED, departmentResponse));
    }

    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<DepartmentDetailResponse>> getDepartmentByName(@PathVariable String name) {
        log.info("Request received to get department details by name: {}", name);
        DepartmentDetailResponse departmentResponse = departmentService.getDepartmentByName(name);
        log.info("Department details retrieved successfully for name: {}", name);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.DEPARTMENTS_FETCHED, departmentResponse));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        log.info("Request received to get all departments");
        List<DepartmentResponse> departmentResponses = departmentService.getAllDepartments();
        log.info("Retrieved {} departments successfully", departmentResponses.size());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.DEPARTMENTS_FETCHED, departmentResponses));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        log.info("Request received to delete department with ID: {}", id);
        departmentService.deleteDepartment(id);
        log.info("Department deleted successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.DEPARTMENT_DELETED));
    }
} 