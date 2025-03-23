package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.mapper.UserMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.CreateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRolesRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.RoleService;
import com.patikadev.definex.advancedtaskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(ErrorMessages.DUPLICATE_EMAIL);
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getDepartmentId() != null) {
            Department department = findDepartmentById(request.getDepartmentId());
            user.setDepartment(department);
        }

        Set<Role> roles;
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            Set<UserRole> defaultRoles = Set.of(UserRole.TEAM_MEMBER);
            roles = roleService.getRolesByNames(defaultRoles);
        } else {
            roles = roleService.getRolesByNames(request.getRoles());
        }
        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUserById(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(ErrorMessages.DUPLICATE_EMAIL);
        }

        userMapper.updateEntityFromDto(request, user);

        if (request.getDepartmentId() != null) {
            Department department = findDepartmentById(request.getDepartmentId());
            user.setDepartment(department);
        }

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(UUID id) {
        User user = findUserById(id);
        return userMapper.toDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserByEmail(String email) {
        User user = findUserByEmail(email);
        return userMapper.toDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAllByIsActiveTrue();
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByDepartmentId(Long departmentId) {
        List<User> users = userRepository.findAllByDepartmentIdAndIsActiveTrue(departmentId);
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        List<User> users = userRepository.findAllByRolesNameAndIsActiveTrue(role);
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = findUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserResponse updateUserRoles(UUID id, UpdateUserRolesRequest request) {
        User user = findUserById(id);

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.USER_ROLE_MUST_BE_SPECIFIED);
        }

        Set<Role> roleEntities = roleService.getRolesByNames(request.getRoles());
        user.setRoles(roleEntities);
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    private User findUserById(UUID id) {
        return userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorMessages.USER_NOT_FOUND.formatted(id)));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorMessages.USER_NOT_FOUND.formatted(email)));
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorMessages.DEPARTMENT_NOT_FOUND.formatted(id)));
    }
} 