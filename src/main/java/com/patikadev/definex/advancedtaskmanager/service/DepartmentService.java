package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.CreateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.UpdateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentDetailResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);

    DepartmentDetailResponse getDepartmentById(Long id);

    DepartmentDetailResponse getDepartmentByName(String name);

    List<DepartmentResponse> getAllDepartments();

    void deleteDepartment(Long id);
} 