package com.patikadev.definex.advancedtaskmanager.mapper;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.CreateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.department.UpdateDepartmentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.department.DepartmentDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Department;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class, ProjectMapper.class})
public interface DepartmentMapper {

    Department toEntity(CreateDepartmentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateDepartmentRequest request, @MappingTarget Department department);

    @Named("toResponse")
    @Mapping(target = "totalUsers", expression = "java((int)department.getUsers().stream().filter(user -> user.getIsActive()).count())")
    @Mapping(target = "totalProjects", expression = "java((int)department.getProjects().stream().filter(project -> project.getIsActive()).count())")
    @Mapping(target = "active", source = "isActive")
    DepartmentResponse toResponse(Department department);

    @Named("toDetailResponse")
    @Mapping(target = "totalUsers", expression = "java((int)department.getUsers().stream().filter(user -> user.getIsActive()).count())")
    @Mapping(target = "totalProjects", expression = "java((int)department.getProjects().stream().filter(project -> project.getIsActive()).count())")
    @Mapping(target = "users", qualifiedByName = "toResponseSet")
    @Mapping(target = "projects", qualifiedByName = "toResponseSet")
    @Mapping(target = "active", source = "isActive")
    DepartmentDetailResponse toDetailResponse(Department department);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    List<DepartmentResponse> toResponseList(List<Department> departments);
}