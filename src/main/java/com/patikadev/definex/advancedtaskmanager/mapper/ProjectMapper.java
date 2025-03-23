package com.patikadev.definex.advancedtaskmanager.mapper;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.CreateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.project.UpdateProjectRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.project.ProjectDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class, TaskMapper.class})
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "teamMembers", source = "teamMembers")
    @Mapping(target = "tasks", ignore = true)
    Project toEntity(CreateProjectRequest request, Department department, Set<User> teamMembers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "teamMembers", ignore = true)
    void updateEntityFromDto(UpdateProjectRequest request, @MappingTarget Project project);

    @Named("toResponse")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "totalTasks", expression = "java((int)project.getTasks().stream().filter(task -> task.getIsActive()).count())")
    @Mapping(target = "completedTasks", expression = "java((int) project.getTasks().stream().filter(task -> task.getIsActive() && task.getState() == com.patikadev.definex.advancedtaskmanager.model.enums.TaskState.COMPLETED).count())")
    @Mapping(target = "active", source = "isActive")
    ProjectResponse toResponse(Project project);

    @Named("toDetailResponse")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "totalTasks", expression = "java((int)project.getTasks().stream().filter(task -> task.getIsActive()).count())")
    @Mapping(target = "completedTasks", expression = "java((int) project.getTasks().stream().filter(task -> task.getIsActive() && task.getState() == com.patikadev.definex.advancedtaskmanager.model.enums.TaskState.COMPLETED).count())")
    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "teamMembers", qualifiedByName = "toResponseSet")
    @Mapping(target = "tasks", qualifiedByName = "toResponseSet")
    ProjectDetailResponse toDetailResponse(Project project);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    List<ProjectResponse> toResponseList(List<Project> project);

    @Named("toResponseSet")
    @IterableMapping(qualifiedByName = "toResponse")
    Set<ProjectResponse> toResponseSet(Set<Project> projects);
} 