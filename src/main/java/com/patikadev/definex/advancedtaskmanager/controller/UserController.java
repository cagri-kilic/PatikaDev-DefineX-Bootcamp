package com.patikadev.definex.advancedtaskmanager.controller;

import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.CreateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRolesRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Request received to create a new user with email: {}", request.getEmail());
        UserResponse userResponse = userService.createUser(request);
        log.info("User created successfully with ID: {}", userResponse.getId());
        return ResponseEntity.ok(ApiResponse.created(SuccessMessages.USER_CREATED, userResponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authServiceImpl.isResourceOwner(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Request received to update user with ID: {}", id);
        UserResponse userResponse = userService.updateUser(id, request);
        log.info("User updated successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USER_UPDATED, userResponse));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable UUID id,
            @RequestBody UpdateUserRolesRequest request) {
        log.info("Request received to update roles for user ID: {} with roles: {}", id, request.getRoles());
        UserResponse userResponse = userService.updateUserRoles(id, request);
        log.info("Roles updated successfully for user ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USER_ROLES_UPDATED, userResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER') or @authServiceImpl.isResourceOwner(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("Request received to get user details for ID: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        log.info("User details retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USER_FETCHED, userResponse));
    }

    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER') or authentication.name == #email")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        log.info("Request received to get user details by email: {}", email);
        UserResponse userResponse = userService.getUserByEmail(email);
        log.info("User details retrieved successfully for email: {}", email);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USER_FETCHED, userResponse));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("Request received to get all users");
        List<UserResponse> userResponses = userService.getAllUsers();
        log.info("Retrieved {} users successfully", userResponses.size());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USERS_FETCHED, userResponses));
    }

    @GetMapping("/by-department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByDepartmentId(@PathVariable Long departmentId) {
        log.info("Request received to get users by department ID: {}", departmentId);
        List<UserResponse> userResponses = userService.getUsersByDepartmentId(departmentId);
        log.info("Retrieved {} users for department ID: {}", userResponses.size(), departmentId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USERS_FETCHED, userResponses));
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable UserRole role) {
        log.info("Request received to get users by role: {}", role);
        List<UserResponse> userResponses = userService.getUsersByRole(role);
        log.info("Retrieved {} users with role: {}", userResponses.size(), role);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USERS_FETCHED, userResponses));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authServiceImpl.isResourceOwner(#id)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        log.info("Request received to delete user with ID: {}", id);
        userService.deleteUser(id);
        log.info("User deleted successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.USER_DELETED));
    }
} 