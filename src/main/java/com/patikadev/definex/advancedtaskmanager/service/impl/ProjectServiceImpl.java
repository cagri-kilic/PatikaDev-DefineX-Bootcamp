package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.exception.UnauthorizedDepartmentAccessException;
import com.patikadev.definex.advancedtaskmanager.mapper.ProjectMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.CreateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectStatusRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.DepartmentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.ProjectRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.AuthService;
import com.patikadev.definex.advancedtaskmanager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final AuthService authService;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Department department = findDepartmentById(request.getDepartmentId());

        validateProjectManagerDepartmentAccess(department.getId());

        Set<User> teamMembers = findUsersByIds(request.getTeamMemberIds());

        Project project = projectMapper.toEntity(request, department, teamMembers);
        Project savedProject = projectRepository.save(project);

        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        Project project = findProjectById(id);

        validateProjectManagerProjectAccess(project);

        projectMapper.updateEntityFromDto(request, project);
        if (request.getDepartmentId() != null) {
            Department department = findDepartmentById(request.getDepartmentId());

            validateProjectManagerDepartmentAccess(department.getId());

            project.setDepartment(department);
        }

        if (request.getTeamMemberIds() != null) {
            Set<User> teamMembers = findUsersByIds(request.getTeamMemberIds());
            project.setTeamMembers(teamMembers);
        }

        Project updatedProject = projectRepository.save(project);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectById(UUID id) {
        Project project = findProjectById(id);
        return projectMapper.toDetailResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAllByIsActiveTrue();
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByDepartmentId(Long departmentId) {
        validateDepartmentExists(departmentId);
        List<Project> projects = projectRepository.findAllByDepartmentIdAndIsActiveTrue(departmentId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByStatus(ProjectStatus status) {
        List<Project> projects = projectRepository.findAllByStatusAndIsActiveTrue(status);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByTeamMemberId(UUID userId) {
        validateUserExists(userId);
        List<Project> projects = projectRepository.findAllByTeamMemberIdAndIsActiveTrue(userId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional
    public ProjectResponse addTeamMember(UUID projectId, UUID userId) {
        Project project = findProjectById(projectId);

        validateProjectManagerProjectAccess(project);

        User user = findUserById(userId);

        if (project.getTeamMembers().contains(user)) {
            throw new IllegalArgumentException(String.format(ErrorMessages.USER_ALREADY_IN_PROJECT, userId));
        }

        project.getTeamMembers().add(user);
        Project updatedProject = projectRepository.save(project);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse removeTeamMember(UUID projectId, UUID userId) {
        Project project = findProjectById(projectId);

        validateProjectManagerProjectAccess(project);

        User user = findUserById(userId);

        if (!project.getTeamMembers().contains(user)) {
            throw new IllegalArgumentException(String.format(ErrorMessages.USER_NOT_IN_PROJECT, userId));
        }

        project.getTeamMembers().remove(user);
        Project updatedProject = projectRepository.save(project);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(UUID id, UpdateProjectStatusRequest request) {
        Project project = findProjectById(id);

        validateProjectManagerProjectAccess(project);

        project.setStatus(request.getNewStatus());

        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(UUID id) {
        Project project = findProjectById(id);

        validateProjectManagerProjectAccess(project);

        if (!project.getTasks().isEmpty()) {
            throw new IllegalStateException(ErrorMessages.PROJECT_HAS_TASKS);
        }

        project.setIsActive(false);
        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return projectRepository.existsByIdAndIsActiveTrue(id);
    }

    private Project findProjectById(UUID id) {
        return projectRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.PROJECT_NOT_FOUND, id)));
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.DEPARTMENT_NOT_FOUND, id)));
    }

    private User findUserById(UUID id) {
        return userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id)));
    }

    private Set<User> findUsersByIds(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashSet<>();
        }

        return userIds.stream()
                .map(this::findUserById)
                .collect(Collectors.toSet());
    }

    private void validateDepartmentExists(Long departmentId) {
        if (!departmentRepository.existsByIdAndIsActiveTrue(departmentId)) {
            throw new ResourceNotFoundException(String.format(ErrorMessages.DEPARTMENT_NOT_FOUND, departmentId));
        }
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsByIdAndIsActiveTrue(userId)) {
            throw new ResourceNotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, userId));
        }
    }

    private void validateProjectManagerDepartmentAccess(Long departmentId) {
        UserResponse currentUser = authService.getCurrentUser();
        Set<UserRole> roles = currentUser.getRoles();

        if (isAdminOrProjectGroupManager(roles)) {
            return;
        }

        boolean isProjectManager = roles.contains(UserRole.PROJECT_MANAGER);

        if (isProjectManager && (currentUser.getDepartmentId() == null ||
                !Objects.equals(currentUser.getDepartmentId(), departmentId))) {
            throw new UnauthorizedDepartmentAccessException(
                    String.format(ErrorMessages.UNAUTHORIZED_PROJECT_CREATE, departmentId));
        }
    }

    private void validateProjectManagerProjectAccess(Project project) {
        UserResponse currentUser = authService.getCurrentUser();
        Set<UserRole> roles = currentUser.getRoles();

        if (isAdminOrProjectGroupManager(roles)) {
            return;
        }

        boolean isProjectManager = roles.contains(UserRole.PROJECT_MANAGER);

        if (isProjectManager) {
            if (currentUser.getDepartmentId() == null) {
                throw new UnauthorizedDepartmentAccessException(ErrorMessages.UNAUTHORIZED_DEPARTMENT_ACCESS);
            }

            if (!Objects.equals(project.getDepartment().getId(), currentUser.getDepartmentId())) {
                if (project.getId() != null) {
                    throw new UnauthorizedDepartmentAccessException(
                            String.format(ErrorMessages.UNAUTHORIZED_PROJECT_UPDATE, project.getId()));
                } else {
                    throw new UnauthorizedDepartmentAccessException(ErrorMessages.UNAUTHORIZED_DEPARTMENT_ACCESS);
                }
            }
        }
    }

    private boolean isAdminOrProjectGroupManager(Set<UserRole> roles) {
        return roles.contains(UserRole.ADMIN) || roles.contains(UserRole.PROJECT_GROUP_MANAGER);
    }
} 