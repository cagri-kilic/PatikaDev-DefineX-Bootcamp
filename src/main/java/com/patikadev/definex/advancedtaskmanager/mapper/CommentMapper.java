package com.patikadev.definex.advancedtaskmanager.mapper;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.CreateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.UpdateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.comment.CommentResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Comment;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateCommentRequest request, @MappingTarget Comment comment);

    @Named("toResponse")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskTitle", source = "task.title")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(comment.getUser().getFirstName() + \" \" + comment.getUser().getLastName())")
    @Mapping(target = "active", source = "isActive")
    CommentResponse toResponse(Comment comment);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    List<CommentResponse> toResponseList(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "user", source = "user")
    Comment toEntity(CreateCommentRequest request, Task task, User user);

    @Named("toResponseSet")
    default Set<CommentResponse> toResponseSet(Set<Comment> comments) {
        if (comments == null) {
            return null;
        }
        return comments.stream()
                .filter(Comment::getIsActive)
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
} 