package com.patikadev.definex.advancedtaskmanager.model.entity;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = ValidationMessages.TASK_TITLE_NOT_BLANK)
    @Size(min = 2, max = 100, message = ValidationMessages.TASK_TITLE_SIZE)
    @Pattern(regexp = RegexPatterns.TITLE_PATTERN, message = ValidationMessages.TASK_TITLE_PATTERN)
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = ValidationMessages.USER_STORY_NOT_BLANK)
    @Column(name = "user_story", nullable = false, columnDefinition = "TEXT")
    private String userStory;

    @NotBlank(message = ValidationMessages.ACCEPTANCE_CRITERIA_NOT_BLANK)
    @Column(name = "acceptance_criteria", nullable = false, columnDefinition = "TEXT")
    private String acceptanceCriteria;

    @NotNull(message = ValidationMessages.TASK_STATE)
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    @Builder.Default
    private TaskState state = TaskState.BACKLOG;

    @NotNull(message = ValidationMessages.TASK_PRIORITY)
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TaskPriority priority;

    @Size(max = 500, message = ValidationMessages.STATE_CHANGE_REASON_MAX_SIZE)
    @Column(name = "state_change_reason")
    private String stateChangeReason;

    @NotNull(message = ValidationMessages.TASK_PROJECT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Attachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<TaskStateHistory> stateHistories = new HashSet<>();
} 