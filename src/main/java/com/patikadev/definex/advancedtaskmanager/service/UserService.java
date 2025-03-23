package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.CreateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRolesRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    UserResponse updateUserRoles(UUID id, UpdateUserRolesRequest request);

    UserDetailResponse getUserById(UUID id);

    UserDetailResponse getUserByEmail(String email);

    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByDepartmentId(Long departmentId);

    List<UserResponse> getUsersByRole(UserRole role);

    void deleteUser(UUID id);

    boolean existsByEmail(String email);
} 