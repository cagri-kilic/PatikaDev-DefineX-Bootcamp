package com.patikadev.definex.advancedtaskmanager.model.entity;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.SecurityConstants;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@Table(name = "users")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = ValidationMessages.FIRST_NAME_NOT_BLANK)
    @Size(min = 2, max = 50, message = ValidationMessages.FIRST_NAME_SIZE)
    @Pattern(regexp = RegexPatterns.NAME_PATTERN, message = ValidationMessages.FIRST_NAME_PATTERN)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = ValidationMessages.LAST_NAME_NOT_BLANK)
    @Size(min = 2, max = 50, message = ValidationMessages.LAST_NAME_SIZE)
    @Pattern(regexp = RegexPatterns.NAME_PATTERN, message = ValidationMessages.LAST_NAME_PATTERN)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = ValidationMessages.EMAIL_NOT_BLANK)
    @Email(message = ValidationMessages.FIELD_EMAIL)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = ValidationMessages.PASSWORD_NOT_BLANK)
    @Size(min = SecurityConstants.MIN_PASSWORD_LENGTH, message = ValidationMessages.PASSWORD_MIN_LENGTH)
    @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN, message = ValidationMessages.PASSWORD_PATTERN)
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany(mappedBy = "teamMembers", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();
} 