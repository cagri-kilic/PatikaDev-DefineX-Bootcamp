package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.RoleRepository;
import com.patikadev.definex.advancedtaskmanager.service.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getRoleByName(UserRole name) {
        return roleRepository.findByNameAndIsActiveTrue(name)
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorMessages.RESOURCE_NOT_FOUND.formatted("Role with name: " + name)));
    }

    @Override
    public Set<Role> getRolesByNames(Set<UserRole> names) {
        return names.stream()
                .map(this::getRoleByName)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @PostConstruct
    @Transactional
    public void initializeRoles() {
        createRoleIfNotExists(UserRole.ADMIN);
        createRoleIfNotExists(UserRole.PROJECT_GROUP_MANAGER);
        createRoleIfNotExists(UserRole.PROJECT_MANAGER);
        createRoleIfNotExists(UserRole.TEAM_LEADER);
        createRoleIfNotExists(UserRole.TEAM_MEMBER);
    }

    private void createRoleIfNotExists(UserRole name) {
        roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    return roleRepository.save(role);
                });
    }
} 