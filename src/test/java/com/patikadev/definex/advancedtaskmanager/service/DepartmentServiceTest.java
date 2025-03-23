package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.mapper.DepartmentMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.CreateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.UpdateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
import com.patikadev.definex.advancedtaskmanager.service.impl.DepartmentServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private CreateDepartmentRequest createDepartmentRequest;
    private UpdateDepartmentRequest updateDepartmentRequest;
    private DepartmentResponse departmentResponse;
    private DepartmentDetailResponse departmentDetailResponse;
    private List<Department> departmentList;
    private List<DepartmentResponse> departmentResponseList;

    @BeforeEach
    void setUp() {
        department = createDepartment();
        createDepartmentRequest = createCreateDepartmentRequest();
        updateDepartmentRequest = createUpdateDepartmentRequest();
        departmentResponse = createDepartmentResponse();
        departmentDetailResponse = createDepartmentDetailResponse();
        departmentList = createDepartmentList();
        departmentResponseList = createDepartmentResponseList();
    }

    @Test
    @DisplayName("Create Department - Success")
    void createDepartment_Success() {
        when(departmentRepository.findByNameAndIsActiveTrue(anyString())).thenReturn(Optional.empty());
        when(departmentMapper.toEntity(any(CreateDepartmentRequest.class))).thenReturn(department);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(departmentMapper.toResponse(any(Department.class))).thenReturn(departmentResponse);

        DepartmentResponse result = departmentService.createDepartment(createDepartmentRequest);

        assertNotNull(result);
        assertEquals(departmentResponse, result);
        verify(departmentRepository).findByNameAndIsActiveTrue(createDepartmentRequest.getName());
        verify(departmentMapper).toEntity(createDepartmentRequest);
        verify(departmentRepository).save(department);
        verify(departmentMapper).toResponse(department);
    }

    @Test
    @DisplayName("Create Department - Name Already Exists")
    void createDepartment_NameAlreadyExists() {
        when(departmentRepository.findByNameAndIsActiveTrue(anyString())).thenReturn(Optional.of(department));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> departmentService.createDepartment(createDepartmentRequest));

        assertEquals(ErrorMessages.DEPARTMENT_NAME_EXISTS.formatted(createDepartmentRequest.getName()),
                exception.getMessage());
        verify(departmentRepository).findByNameAndIsActiveTrue(createDepartmentRequest.getName());
        verify(departmentMapper, never()).toEntity(any(CreateDepartmentRequest.class));
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Update Department - Success")
    void updateDepartment_Success() {
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(departmentMapper.toResponse(any(Department.class))).thenReturn(departmentResponse);

        DepartmentResponse result = departmentService.updateDepartment(1L, updateDepartmentRequest);

        assertNotNull(result);
        assertEquals(departmentResponse, result);
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentMapper).updateEntityFromDto(updateDepartmentRequest, department);
        verify(departmentRepository).save(department);
        verify(departmentMapper).toResponse(department);
    }

    @Test
    @DisplayName("Update Department - Not Found")
    void updateDepartment_NotFound() {
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> departmentService.updateDepartment(1L, updateDepartmentRequest));

        assertTrue(exception.getMessage().contains(String.valueOf(1L)));
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentMapper, never()).updateEntityFromDto(any(), any());
        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Department - Name Already Exists")
    void updateDepartment_NameAlreadyExists() {
        Department existingDepartment = createDepartment();
        existingDepartment.setId(2L);
        existingDepartment.setName("New Department");

        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(departmentRepository.findByNameAndIsActiveTrue(anyString())).thenReturn(Optional.of(existingDepartment));

        updateDepartmentRequest.setName("New Department");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> departmentService.updateDepartment(1L, updateDepartmentRequest));

        assertEquals(ErrorMessages.DEPARTMENT_NAME_EXISTS.formatted(updateDepartmentRequest.getName()),
                exception.getMessage());
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentRepository).findByNameAndIsActiveTrue(updateDepartmentRequest.getName());
        verify(departmentMapper, never()).updateEntityFromDto(any(), any());
        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Department By Id - Success")
    void getDepartmentById_Success() {
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));
        when(departmentMapper.toDetailResponse(any(Department.class))).thenReturn(departmentDetailResponse);

        DepartmentDetailResponse result = departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals(departmentDetailResponse, result);
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentMapper).toDetailResponse(department);
    }

    @Test
    @DisplayName("Get Department By Id - Not Found")
    void getDepartmentById_NotFound() {
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(1L));

        assertTrue(exception.getMessage().contains(String.valueOf(1L)));
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Get Department By Name - Success")
    void getDepartmentByName_Success() {
        when(departmentRepository.findByNameAndIsActiveTrue(anyString())).thenReturn(Optional.of(department));
        when(departmentMapper.toDetailResponse(any(Department.class))).thenReturn(departmentDetailResponse);

        DepartmentDetailResponse result = departmentService.getDepartmentByName("IT Department");

        assertNotNull(result);
        assertEquals(departmentDetailResponse, result);
        verify(departmentRepository).findByNameAndIsActiveTrue("IT Department");
        verify(departmentMapper).toDetailResponse(department);
    }

    @Test
    @DisplayName("Get Department By Name - Not Found")
    void getDepartmentByName_NotFound() {
        when(departmentRepository.findByNameAndIsActiveTrue(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> departmentService.getDepartmentByName("IT Department"));

        assertTrue(exception.getMessage().contains("IT Department"));
        verify(departmentRepository).findByNameAndIsActiveTrue("IT Department");
        verify(departmentMapper, never()).toDetailResponse(any());
    }

    @Test
    @DisplayName("Get All Departments - Success")
    void getAllDepartments_Success() {
        when(departmentRepository.findAllByIsActiveTrue()).thenReturn(departmentList);
        when(departmentMapper.toResponseList(anyList())).thenReturn(departmentResponseList);

        List<DepartmentResponse> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(departmentResponseList, result);
        assertEquals(departmentResponseList.size(), result.size());
        verify(departmentRepository).findAllByIsActiveTrue();
        verify(departmentMapper).toResponseList(departmentList);
    }

    @Test
    @DisplayName("Delete Department - Success")
    void deleteDepartment_Success() {
        Department emptyDepartment = createDepartment();
        emptyDepartment.setUsers(new HashSet<>());
        emptyDepartment.setProjects(new HashSet<>());

        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(emptyDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(emptyDepartment);

        departmentService.deleteDepartment(1L);

        assertFalse(emptyDepartment.getIsActive());
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentRepository).save(emptyDepartment);
    }

    @Test
    @DisplayName("Delete Department - Not Found")
    void deleteDepartment_NotFound() {
        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> departmentService.deleteDepartment(1L));

        assertTrue(exception.getMessage().contains(String.valueOf(1L)));
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete Department - Has Users")
    void deleteDepartment_HasUsers() {
        Set<User> users = new HashSet<>();
        users.add(new User());
        department.setUsers(users);
        department.setProjects(new HashSet<>());

        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> departmentService.deleteDepartment(1L));

        assertEquals(ErrorMessages.DEPARTMENT_HAS_USERS, exception.getMessage());
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete Department - Has Projects")
    void deleteDepartment_HasProjects() {
        department.setUsers(new HashSet<>());
        Set<Project> projects = new HashSet<>();
        projects.add(new Project());
        department.setProjects(projects);

        when(departmentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(department));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> departmentService.deleteDepartment(1L));

        assertEquals(ErrorMessages.DEPARTMENT_HAS_PROJECTS, exception.getMessage());
        verify(departmentRepository).findByIdAndIsActiveTrue(1L);
        verify(departmentRepository, never()).save(any());
    }

    private Department createDepartment() {
        Department department = Department.builder()
                .id(1L)
                .name("IT Department")
                .description("Information Technology Department")
                .build();
        department.setIsActive(true);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        return department;
    }

    private CreateDepartmentRequest createCreateDepartmentRequest() {
        CreateDepartmentRequest request = new CreateDepartmentRequest();
        request.setName("IT Department");
        request.setDescription("Information Technology Department");
        return request;
    }

    private UpdateDepartmentRequest createUpdateDepartmentRequest() {
        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setName("Updated IT Department");
        request.setDescription("Updated Information Technology Department");
        return request;
    }

    private DepartmentResponse createDepartmentResponse() {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(1L);
        response.setName("IT Department");
        response.setDescription("Information Technology Department");
        response.setTotalUsers(0);
        response.setTotalProjects(0);
        response.setActive(true);
        return response;
    }

    private DepartmentDetailResponse createDepartmentDetailResponse() {
        DepartmentDetailResponse response = new DepartmentDetailResponse();
        response.setId(1L);
        response.setName("IT Department");
        response.setDescription("Information Technology Department");
        response.setTotalUsers(0);
        response.setTotalProjects(0);
        response.setActive(true);
        response.setUsers(new HashSet<>());
        response.setProjects(new HashSet<>());
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    private List<Department> createDepartmentList() {
        return Collections.singletonList(department);
    }

    private List<DepartmentResponse> createDepartmentResponseList() {
        return Collections.singletonList(departmentResponse);
    }
} 