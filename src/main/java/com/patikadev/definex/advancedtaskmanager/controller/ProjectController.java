package com.patikadev.definex.advancedtaskmanager.controller;

import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.CreateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectStatusRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectResponse;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import com.patikadev.definex.advancedtaskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        log.info("Request received to create a new project with title: {}", request.getTitle());
        ProjectResponse response = projectService.createProject(request);
        log.info("Project created successfully with ID: {}", response.getId());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECT_CREATED, response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        log.info("Request received to update project with ID: {}", id);
        ProjectResponse response = projectService.updateProject(id, request);
        log.info("Project updated successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECT_UPDATED, response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectById(@PathVariable UUID id) {
        log.info("Request received to get project details for ID: {}", id);
        ProjectDetailResponse response = projectService.getProjectById(id);
        log.info("Project details retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECT_RETRIEVED, response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects() {
        log.info("Request received to get all projects");
        List<ProjectResponse> responses = projectService.getAllProjects();
        log.info("Retrieved {} projects successfully", responses.size());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECTS_RETRIEVED, responses));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByDepartmentId(@PathVariable Long departmentId) {
        log.info("Request received to get projects for department ID: {}", departmentId);
        List<ProjectResponse> responses = projectService.getProjectsByDepartmentId(departmentId);
        log.info("Retrieved {} projects for department ID: {}", responses.size(), departmentId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECTS_RETRIEVED, responses));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByStatus(@PathVariable ProjectStatus status) {
        log.info("Request received to get projects by status: {}", status);
        List<ProjectResponse> responses = projectService.getProjectsByStatus(status);
        log.info("Retrieved {} projects with status: {}", responses.size(), status);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECTS_RETRIEVED, responses));
    }

    @GetMapping("/team-member/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByTeamMemberId(@PathVariable UUID userId) {
        log.info("Request received to get projects for team member ID: {}", userId);
        List<ProjectResponse> responses = projectService.getProjectsByTeamMemberId(userId);
        log.info("Retrieved {} projects for team member ID: {}", responses.size(), userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECTS_RETRIEVED, responses));
    }

    @PostMapping("/{projectId}/team-members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> addTeamMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        log.info("Request received to add team member ID: {} to project ID: {}", userId, projectId);
        ProjectResponse response = projectService.addTeamMember(projectId, userId);
        log.info("Team member ID: {} successfully added to project ID: {}", userId, projectId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TEAM_MEMBER_ADDED, response));
    }

    @DeleteMapping("/{projectId}/team-members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> removeTeamMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        log.info("Request received to remove team member ID: {} from project ID: {}", userId, projectId);
        ProjectResponse response = projectService.removeTeamMember(projectId, userId);
        log.info("Team member ID: {} successfully removed from project ID: {}", userId, projectId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.TEAM_MEMBER_REMOVED, response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectStatusRequest request) {
        log.info("Request received to update project status for ID: {} to status: {}", id, request.getNewStatus());
        ProjectResponse response = projectService.updateProjectStatus(id, request);
        log.info("Project status updated successfully for ID: {} to status: {}", id, request.getNewStatus());
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECT_STATUS_UPDATED, response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        log.info("Request received to delete project ID: {}", id);
        projectService.deleteProject(id);
        log.info("Project deleted successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.PROJECT_DELETED));
    }
} 