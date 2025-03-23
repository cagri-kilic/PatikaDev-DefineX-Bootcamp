package com.patikadev.definex.advancedtaskmanager.model.entity;

import com.patikadev.definex.advancedtaskmanager.constant.FileConstants;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attachments")
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @NotBlank(message = ValidationMessages.FILE_NAME_NOT_BLANK)
    @Size(max = FileConstants.MAX_FILE_NAME_LENGTH, message = ValidationMessages.FILE_NAME_MAX_SIZE)
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotBlank(message = ValidationMessages.FILE_PATH)
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = FileConstants.MAX_CONTENT_TYPE_LENGTH, message = ValidationMessages.CONTENT_TYPE_MAX_SIZE)
    @Column(name = "content_type")
    private String contentType;

    @NotNull(message = ValidationMessages.FILE_TASK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @NotNull(message = ValidationMessages.FILE_UPLOADER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedByUser;
} 