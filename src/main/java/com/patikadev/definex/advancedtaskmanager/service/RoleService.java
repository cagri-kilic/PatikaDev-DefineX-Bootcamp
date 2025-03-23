package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;

import java.util.List;
import java.util.Set;

public interface RoleService {

    Role getRoleByName(UserRole name);

    Set<Role> getRolesByNames(Set<UserRole> names);

    List<Role> getAllRoles();

    void initializeRoles();
} 