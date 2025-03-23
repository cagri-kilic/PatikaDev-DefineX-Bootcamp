package com.patikadev.definex.advancedtaskmanager.mapper;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.auth.RegisterRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.CreateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.user.UpdateUserRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.user.UserDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateUserRequest request, @MappingTarget User user);

    @Named("toResponse")
    @Mapping(target = "roles", expression = "java(mapRolesToUserRoles(user.getRoles()))")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "active", source = "isActive")
    UserResponse toResponse(User user);

    @Named("toDetailResponse")
    @Mapping(target = "roles", expression = "java(mapRolesToUserRoles(user.getRoles()))")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "active", source = "isActive")
    UserDetailResponse toDetailResponse(User user);

    @Mapping(target = "roles", ignore = true)
    CreateUserRequest toCreateUserRequest(RegisterRequest request);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    List<UserResponse> toResponseList(List<User> users);

    @Named("toResponseSet")
    default Set<UserResponse> toResponseSet(Set<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .filter(User::getIsActive)
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    default Set<UserRole> mapRolesToUserRoles(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
} 