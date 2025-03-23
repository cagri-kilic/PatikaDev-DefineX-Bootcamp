package com.patikadev.definex.advancedtaskmanager.model.entity;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class Project extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = ValidationMessages.PROJECT_TITLE_NOT_BLANK)
    @Size(min = 2, max = 100, message = ValidationMessages.PROJECT_TITLE_SIZE)
    @Pattern(regexp = RegexPatterns.TITLE_PATTERN, message = ValidationMessages.PROJECT_TITLE_PATTERN)
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 1000, message = ValidationMessages.PROJECT_DESCRIPTION_MAX_SIZE)
    @Column(name = "description")
    private String description;

    @NotNull(message = ValidationMessages.PROJECT_STATUS_MUST_BE_SPECIFIED)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PENDING;

    @NotNull(message = ValidationMessages.PROJECT_DEPARTMENT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> teamMembers = new HashSet<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();
} 