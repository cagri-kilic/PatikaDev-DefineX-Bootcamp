package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.RoleRepository;
import com.patikadev.definex.advancedtaskmanager.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role adminRole;
    private Role projectManagerRole;
    private Role teamLeaderRole;
    private List<Role> allRoles;
    private Set<UserRole> roleNames;

    @BeforeEach
    void setUp() {
        adminRole = createRole(1L, UserRole.ADMIN);
        projectManagerRole = createRole(2L, UserRole.PROJECT_MANAGER);
        teamLeaderRole = createRole(3L, UserRole.TEAM_LEADER);

        allRoles = createAllRoles();
        roleNames = createRoleNames();
    }

    @Test
    @DisplayName("Get Role By Name - Success")
    void getRoleByName_Success() {
        when(roleRepository.findByNameAndIsActiveTrue(any(UserRole.class))).thenReturn(Optional.of(adminRole));

        Role result = roleService.getRoleByName(UserRole.ADMIN);

        assertNotNull(result);
        assertEquals(adminRole.getId(), result.getId());
        assertEquals(adminRole.getName(), result.getName());
        verify(roleRepository).findByNameAndIsActiveTrue(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Get Role By Name - Not Found")
    void getRoleByName_NotFound() {
        when(roleRepository.findByNameAndIsActiveTrue(any(UserRole.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roleService.getRoleByName(UserRole.ADMIN));

        assertTrue(exception.getMessage().contains(UserRole.ADMIN.toString()));
        verify(roleRepository).findByNameAndIsActiveTrue(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Get Roles By Names - Success")
    void getRolesByNames_Success() {
        when(roleRepository.findByNameAndIsActiveTrue(UserRole.ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByNameAndIsActiveTrue(UserRole.PROJECT_MANAGER)).thenReturn(Optional.of(projectManagerRole));

        Set<Role> result = roleService.getRolesByNames(roleNames);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(adminRole));
        assertTrue(result.contains(projectManagerRole));
        verify(roleRepository, times(1)).findByNameAndIsActiveTrue(UserRole.ADMIN);
        verify(roleRepository, times(1)).findByNameAndIsActiveTrue(UserRole.PROJECT_MANAGER);
    }

    @Test
    @DisplayName("Get All Roles - Success")
    void getAllRoles_Success() {
        when(roleRepository.findAll()).thenReturn(allRoles);

        List<Role> result = roleService.getAllRoles();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(allRoles, result);
        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("Initialize Roles - All Roles Are Created")
    void initializeRoles_AllRolesAreCreated() {
        when(roleRepository.findByName(any(UserRole.class))).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role savedRole = invocation.getArgument(0);
            savedRole.setId(new Random().nextLong());
            return savedRole;
        });

        roleService.initializeRoles();

        verify(roleRepository, times(5)).findByName(any(UserRole.class));
        verify(roleRepository, times(5)).save(any(Role.class));
    }

    @Test
    @DisplayName("Initialize Roles - Some Roles Already Exist")
    void initializeRoles_SomeRolesAlreadyExist() {
        when(roleRepository.findByName(any(UserRole.class))).thenReturn(Optional.empty());
        when(roleRepository.findByName(UserRole.ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(UserRole.PROJECT_MANAGER)).thenReturn(Optional.of(projectManagerRole));

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role savedRole = invocation.getArgument(0);
            savedRole.setId(new Random().nextLong());
            return savedRole;
        });

        roleService.initializeRoles();

        verify(roleRepository, times(5)).findByName(any(UserRole.class));
        verify(roleRepository, times(3)).save(any(Role.class));
    }

    private Role createRole(Long id, UserRole userRole) {
        Role role = Role.builder()
                .id(id)
                .name(userRole)
                .build();
        role.setIsActive(true);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return role;
    }

    private List<Role> createAllRoles() {
        return Arrays.asList(adminRole, projectManagerRole, teamLeaderRole);
    }

    private Set<UserRole> createRoleNames() {
        Set<UserRole> names = new HashSet<>();
        names.add(UserRole.ADMIN);
        names.add(UserRole.PROJECT_MANAGER);
        return names;
    }
} 