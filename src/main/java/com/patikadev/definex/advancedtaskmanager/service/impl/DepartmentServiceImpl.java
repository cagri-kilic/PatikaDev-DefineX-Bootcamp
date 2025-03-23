package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.mapper.DepartmentMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.CreateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.UpdateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
import com.patikadev.definex.advancedtaskmanager.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        checkIfDepartmentNameExists(request.getName());

        Department department = departmentMapper.toEntity(request);
        Department savedDepartment = departmentRepository.save(department);

        return departmentMapper.toResponse(savedDepartment);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        Department department = findDepartmentById(id);

        if (request.getName() != null && !request.getName().equals(department.getName())) {
            checkIfDepartmentNameExists(request.getName());
        }

        departmentMapper.updateEntityFromDto(request, department);
        Department updatedDepartment = departmentRepository.save(department);

        return departmentMapper.toResponse(updatedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDetailResponse getDepartmentById(Long id) {
        Department department = findDepartmentById(id);
        return departmentMapper.toDetailResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDetailResponse getDepartmentByName(String name) {
        Department department = findDepartmentByName(name);
        return departmentMapper.toDetailResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        List<Department> departments = departmentRepository.findAllByIsActiveTrue();
        return departmentMapper.toResponseList(departments);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = findDepartmentById(id);

        if (!department.getUsers().isEmpty()) {
            throw new IllegalStateException(ErrorMessages.DEPARTMENT_HAS_USERS);
        }

        if (!department.getProjects().isEmpty()) {
            throw new IllegalStateException(ErrorMessages.DEPARTMENT_HAS_PROJECTS);
        }

        department.setIsActive(false);
        departmentRepository.save(department);
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DEPARTMENT_NOT_FOUND.formatted(id)));
    }

    private Department findDepartmentByName(String name) {
        return departmentRepository.findByNameAndIsActiveTrue(name)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DEPARTMENT_NOT_FOUND.formatted(name)));
    }

    private void checkIfDepartmentNameExists(String name) {
        Optional<Department> existingDepartment = departmentRepository.findByNameAndIsActiveTrue(name);
        if (existingDepartment.isPresent()) {
            throw new IllegalArgumentException(ErrorMessages.DEPARTMENT_NAME_EXISTS.formatted(name));
        }
    }
} 