package com.patikadev.definex.advancedtaskmanager.model.entity;

import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_state_histories")
public class TaskStateHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_state")
    private TaskState oldState;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "new_state", nullable = false)
    private TaskState newState;

    @Size(max = 500, message = ValidationMessages.STATE_CHANGE_REASON_MAX_SIZE)
    @Column(name = "reason")
    private String reason;

    @NotNull
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
} 