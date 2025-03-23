package com.patikadev.definex.advancedtaskmanager.mapper;

import com.patikadev.definex.advancedtaskmanager.model.dto.response.attachment.AttachmentResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Attachment;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttachmentMapper {

    @Named("toResponse")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskTitle", source = "task.title")
    @Mapping(target = "uploadedByUserId", source = "uploadedByUser.id")
    @Mapping(target = "uploadedByUserName", expression = "java(attachment.getUploadedByUser().getFirstName() + \" \" + attachment.getUploadedByUser().getLastName())")
    @Mapping(target = "active", source = "isActive")
    AttachmentResponse toResponse(Attachment attachment);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    List<AttachmentResponse> toResponseList(List<Attachment> attachments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "uploadedByUser", source = "user")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "filePath", source = "filePath")
    @Mapping(target = "fileSize", source = "fileSize")
    @Mapping(target = "contentType", source = "contentType")
    Attachment toEntity(String fileName, Task task, User user, String filePath, Long fileSize, String contentType);

    @Named("toResponseSet")
    default Set<AttachmentResponse> toResponseSet(Set<Attachment> attachments) {
        if (attachments == null) {
            return null;
        }
        return attachments.stream()
                .filter(Attachment::getIsActive)
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
} 