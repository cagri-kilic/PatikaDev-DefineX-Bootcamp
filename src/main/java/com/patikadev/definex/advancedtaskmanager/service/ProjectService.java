package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.CreateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectStatusRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectResponse;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse updateProject(UUID id, UpdateProjectRequest request);

    ProjectDetailResponse getProjectById(UUID id);

    List<ProjectResponse> getAllProjects();

    List<ProjectResponse> getProjectsByDepartmentId(Long departmentId);

    List<ProjectResponse> getProjectsByStatus(ProjectStatus status);

    List<ProjectResponse> getProjectsByTeamMemberId(UUID userId);

    ProjectResponse addTeamMember(UUID projectId, UUID userId);

    ProjectResponse removeTeamMember(UUID projectId, UUID userId);

    ProjectResponse updateProjectStatus(UUID id, UpdateProjectStatusRequest request);

    void deleteProject(UUID id);

    boolean existsById(UUID id);
} 