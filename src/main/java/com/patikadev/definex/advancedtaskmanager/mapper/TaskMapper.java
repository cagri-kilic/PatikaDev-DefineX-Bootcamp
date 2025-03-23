package com.patikadev.definex.advancedtaskmanager.mapper;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.CreateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.task.UpdateTaskRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.task.TaskDetailResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CommentMapper.class, AttachmentMapper.class, TaskStateHistoryMapper.class})
public interface TaskMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateTaskRequest request, @MappingTarget Task task);

    @Named("toResponse")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectTitle", source = "project.title")
    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    @Mapping(target = "assignedUserName", expression = "java(task.getAssignedUser() != null ? task.getAssignedUser().getFirstName() + \" \" + task.getAssignedUser().getLastName() : null)")
    @Mapping(target = "totalComments", expression = "java((int)task.getComments().stream().filter(comment -> comment.getIsActive()).count())")
    @Mapping(target = "totalAttachments", expression = "java((int)task.getAttachments().stream().filter(attachment -> attachment.getIsActive()).count())")
    @Mapping(target = "active", source = "isActive")
    TaskResponse toResponse(Task task);

    @Named("toDetailResponse")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectTitle", source = "project.title")
    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    @Mapping(target = "assignedUserName", expression = "java(task.getAssignedUser() != null ? task.getAssignedUser().getFirstName() + \" \" + task.getAssignedUser().getLastName() : null)")
    @Mapping(target = "totalComments", expression = "java((int)task.getComments().stream().filter(comment -> comment.getIsActive()).count())")
    @Mapping(target = "totalAttachments", expression = "java((int)task.getAttachments().stream().filter(attachment -> attachment.getIsActive()).count())")
    @Mapping(target = "comments", qualifiedByName = "toResponseSet")
    @Mapping(target = "attachments", qualifiedByName = "toResponseSet")
    @Mapping(target = "stateHistories", qualifiedByName = "toResponseSet")
    @Mapping(target = "active", source = "isActive")
    TaskDetailResponse toDetailResponse(Task task);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    List<TaskResponse> toResponseList(List<Task> tasks);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "assignedUser", source = "assignedUser")
    @Mapping(target = "state", constant = "BACKLOG")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    Task toEntity(CreateTaskRequest request, Project project, User assignedUser);

    @Named("toResponseSet")
    default Set<TaskResponse> toResponseSet(Set<Task> tasks) {
        if (tasks == null) {
            return null;
        }
        return tasks.stream()
                .filter(Task::getIsActive)
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
} 