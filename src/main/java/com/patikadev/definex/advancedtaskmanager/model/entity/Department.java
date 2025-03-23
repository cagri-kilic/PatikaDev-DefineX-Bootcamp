package com.patikadev.definex.advancedtaskmanager.model.entity;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "departments")
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @NotBlank(message = ValidationMessages.DEPARTMENT_NAME_NOT_BLANK)
    @Size(min = 2, max = 100, message = ValidationMessages.DEPARTMENT_NAME_SIZE)
    @Pattern(regexp = RegexPatterns.DEPARTMENT_NAME_PATTERN, message = ValidationMessages.DEPARTMENT_NAME_PATTERN)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Size(max = 500, message = ValidationMessages.DEPARTMENT_DESCRIPTION_MAX_SIZE)
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();
} 