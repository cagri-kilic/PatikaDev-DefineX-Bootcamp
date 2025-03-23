package com.patikadev.definex.advancedtaskmanager.service;

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
import com.patikadev.definex.advancedtaskmanager.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Department department;
    private Role role;
    private Set<Role> roles;
    private UUID userId;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;
    private UpdateUserRolesRequest updateUserRolesRequest;
    private UserResponse userResponse;
    private UserDetailResponse userDetailResponse;
    private List<User> userList;
    private List<UserResponse> userResponseList;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = createUser();
        department = createDepartment();
        role = createRole();
        roles = createRoleSet();
        user.setRoles(roles);
        user.setDepartment(department);
        createUserRequest = createCreateUserRequest(UserRole.PROJECT_MANAGER);
        updateUserRequest = createUpdateUserRequest();
        updateUserRolesRequest = createUpdateUserRolesRequest();
        userResponse = createUserResponse();
        userDetailResponse = createUserDetailResponse();
        userList = createUserList();
        userResponseList = createUserResponseList();
    }

    @Test
    @DisplayName("Create User - Success")
    void createUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(CreateUserRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(roleService.getRolesByNames(any())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.createUser(createUserRequest);

        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userMapper).toEntity(createUserRequest);
        verify(passwordEncoder).encode(createUserRequest.getPassword());
        verify(departmentRepository).findByIdAndIsActiveTrue(createUserRequest.getDepartmentId());
        verify(roleService).getRolesByNames(createUserRequest.getRoles());
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("Create User - Duplicate Email")
    void createUser_DuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(createUserRequest));
        assertEquals(ErrorMessages.DUPLICATE_EMAIL, exception.getMessage());
        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userMapper, never()).toEntity(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Create User - Default Role")
    void createUser_DefaultRole() {
        CreateUserRequest request = createCreateUserRequest(UserRole.TEAM_MEMBER);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(CreateUserRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(roleService.getRolesByNames(any())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(roleService).getRolesByNames(any());
    }

    @Test
    @DisplayName("Update User - Success")
    void updateUser_Success() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.updateUser(userId, updateUserRequest);

        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(userMapper).updateEntityFromDto(updateUserRequest, user);
        verify(departmentRepository).findByIdAndIsActiveTrue(updateUserRequest.getDepartmentId());
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("Update User - Not Found")
    void updateUser_UserNotFound() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userId, updateUserRequest));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(userMapper, never()).updateEntityFromDto(any(), any());
    }

    @Test
    @DisplayName("Update User - Duplicate Email")
    void updateUser_DuplicateEmail() {
        updateUserRequest.setEmail("new@example.com");
        user.setEmail("old@example.com");

        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userId, updateUserRequest));
        assertEquals(ErrorMessages.DUPLICATE_EMAIL, exception.getMessage());
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(userRepository).existsByEmail(updateUserRequest.getEmail());
        verify(userMapper, never()).updateEntityFromDto(any(), any());
    }

    @Test
    @DisplayName("Get User By Id - Success")
    void getUserById_Success() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(userMapper.toDetailResponse(any(User.class))).thenReturn(userDetailResponse);

        UserDetailResponse result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userDetailResponse, result);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(userMapper).toDetailResponse(user);
    }

    @Test
    @DisplayName("Get User By Id - Not Found")
    void getUserById_UserNotFound() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(userId));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(userMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Get User By Email - Success")
    void getUserByEmail_Success() {
        String email = "test@example.com";
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toDetailResponse(any(User.class))).thenReturn(userDetailResponse);

        UserDetailResponse result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(userDetailResponse, result);
        verify(userRepository).findByEmailAndIsActiveTrue(email);
        verify(userMapper).toDetailResponse(user);
    }

    @Test
    @DisplayName("Get User By Email - Not Found")
    void getUserByEmail_UserNotFound() {
        String email = "test@example.com";
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail(email));
        assertTrue(exception.getMessage().contains(email));
        verify(userRepository).findByEmailAndIsActiveTrue(email);
        verify(userMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Get All Users - Success")
    void getAllUsers_Success() {
        when(userRepository.findAllByIsActiveTrue()).thenReturn(userList);
        when(userMapper.toResponseList(anyList())).thenReturn(userResponseList);

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(userResponseList, result);
        assertEquals(userResponseList.size(), result.size());
        verify(userRepository).findAllByIsActiveTrue();
        verify(userMapper).toResponseList(userList);
    }

    @Test
    @DisplayName("Get Users By Department Id - Success")
    void getUsersByDepartmentId_Success() {
        Long departmentId = 1L;
        when(userRepository.findAllByDepartmentIdAndIsActiveTrue(anyLong())).thenReturn(userList);
        when(userMapper.toResponseList(anyList())).thenReturn(userResponseList);

        List<UserResponse> result = userService.getUsersByDepartmentId(departmentId);

        assertNotNull(result);
        assertEquals(userResponseList, result);
        assertEquals(userResponseList.size(), result.size());
        verify(userRepository).findAllByDepartmentIdAndIsActiveTrue(departmentId);
        verify(userMapper).toResponseList(userList);
    }

    @Test
    @DisplayName("Get Users By Role - Success")
    void getUsersByRole_Success() {
        UserRole userRole = UserRole.TEAM_MEMBER;
        when(userRepository.findAllByRolesNameAndIsActiveTrue(any(UserRole.class))).thenReturn(userList);
        when(userMapper.toResponseList(anyList())).thenReturn(userResponseList);

        List<UserResponse> result = userService.getUsersByRole(userRole);

        assertNotNull(result);
        assertEquals(userResponseList, result);
        assertEquals(userResponseList.size(), result.size());
        verify(userRepository).findAllByRolesNameAndIsActiveTrue(userRole);
        verify(userMapper).toResponseList(userList);
    }

    @Test
    @DisplayName("Delete User - Success")
    void deleteUser_Success() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.deleteUser(userId);

        assertFalse(user.getIsActive());
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Delete User - Not Found")
    void deleteUser_UserNotFound() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(userId));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Exists By Email - True")
    void existsByEmail_True() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        boolean result = userService.existsByEmail(email);

        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Exists By Email - False")
    void existsByEmail_False() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        boolean result = userService.existsByEmail(email);

        assertFalse(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Update User Roles - Success")
    void updateUserRoles_Success() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(roleService.getRolesByNames(any())).thenReturn(roles);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.updateUserRoles(userId, updateUserRolesRequest);

        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(roleService).getRolesByNames(updateUserRolesRequest.getRoles());
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("Update User Roles - User Not Found")
    void updateUserRoles_UserNotFound() {
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserRoles(userId, updateUserRolesRequest));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(roleService, never()).getRolesByNames(any());
    }

    @Test
    @DisplayName("Update User Roles - Empty Roles")
    void updateUserRoles_EmptyRoles() {
        UpdateUserRolesRequest emptyRolesRequest = new UpdateUserRolesRequest();
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserRoles(userId, emptyRolesRequest));
        assertEquals(ErrorMessages.USER_ROLE_MUST_BE_SPECIFIED, exception.getMessage());
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(roleService, never()).getRolesByNames(any());
    }

    private User createUser() {
        User newUser = User.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .password("Password123!")
                .build();
        newUser.setIsActive(true);
        return newUser;
    }

    private Department createDepartment() {
        Department newDepartment = Department.builder()
                .id(1L)
                .name("IT Department")
                .description("Information Technology Department")
                .build();
        newDepartment.setIsActive(true);
        return newDepartment;
    }

    private Role createRole() {
        return Role.builder()
                .id(1L)
                .name(UserRole.PROJECT_MANAGER)
                .build();
    }

    private Set<Role> createRoleSet() {
        return new HashSet<>(Collections.singletonList(role));
    }

    private CreateUserRequest createCreateUserRequest(UserRole userRole) {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test.user@example.com");
        request.setPassword("Password123!");
        request.setDepartmentId(1L);
        request.setRoles(Set.of(userRole));
        return request;
    }

    private UpdateUserRequest createUpdateUserRequest() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Test Update");
        request.setLastName("User Update");
        request.setEmail("test.update@example.com");
        request.setDepartmentId(1L);
        return request;
    }

    private UpdateUserRolesRequest createUpdateUserRolesRequest() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(Set.of(UserRole.TEAM_LEADER));
        return request;
    }

    private UserResponse createUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(userId);
        response.setFirstName("Test");
        response.setLastName("User");
        response.setEmail("test.user@example.com");
        response.setRoles(Set.of(UserRole.TEAM_MEMBER));
        response.setDepartmentId(1L);
        response.setDepartmentName("IT Department");
        response.setActive(true);
        return response;
    }

    private UserDetailResponse createUserDetailResponse() {
        UserDetailResponse response = new UserDetailResponse();
        response.setId(userId);
        response.setFirstName("Test");
        response.setLastName("User");
        response.setEmail("test.user@example.com");
        response.setRoles(Set.of(UserRole.TEAM_MEMBER));
        response.setDepartmentId(1L);
        response.setDepartmentName("IT Department");
        response.setActive(true);
        response.setCreatedAt(null);
        response.setCreatedBy(null);
        response.setUpdatedAt(null);
        response.setUpdatedBy(null);
        response.setProjects(null);
        return response;
    }

    private List<User> createUserList() {
        return List.of(user);
    }

    private List<UserResponse> createUserResponseList() {
        return List.of(userResponse);
    }
} 