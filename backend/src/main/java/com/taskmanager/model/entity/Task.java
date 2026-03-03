package com.taskmanager.model.entity;

import com.taskmanager.model.enums.TaskPriority;
import com.taskmanager.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task entity represents a single task in the task management system.
 * 
 * Each task has a title, description, status, priority, and due date.
 * Tasks are assigned to users and track their creation and modification times.
 * 
 * The task status follows a Kanban-style workflow:
 * TODO -> IN_PROGRESS -> REVIEW -> COMPLETED
 * 
 * In the frontend, tasks are displayed as cards on a Kanban board,
 * and users can drag-and-drop them between columns to change their status.
 * 
 * We use JPA annotations to define how this entity maps to the database table.
 * The @Entity annotation marks it as a JPA entity, and various @Column
 * annotations configure individual field mappings.
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    /**
     * Primary key - auto-generated unique identifier for each task.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Short title describing the task (e.g., "Fix login bug").
     * This is what appears as the card title on the Kanban board.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Detailed description of what needs to be done.
     * We use @Column(columnDefinition = "TEXT") to allow longer descriptions
     * instead of the default VARCHAR(255) limit.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Current status of the task (TODO, IN_PROGRESS, REVIEW, COMPLETED).
     * Stored as a string in the database for readability.
     * Default value is TODO for newly created tasks.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    /**
     * Priority level (LOW, MEDIUM, HIGH, URGENT).
     * Helps users decide which tasks to work on first.
     * Default value is MEDIUM for newly created tasks.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    /**
     * When this task should be completed by.
     * Can be null if no deadline is set.
     * The frontend shows a warning indicator when a task is overdue.
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * Many-to-one relationship: many tasks can be assigned to one user.
     * The @JoinColumn specifies the foreign key column in the tasks table.
     * FetchType.LAZY means the user data is only loaded from the database
     * when we actually access it, which improves performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /**
     * The user who originally created this task.
     * This is separate from the assignee because a manager might
     * create a task and assign it to someone else.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /**
     * When this task was created. Set automatically and never updated.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * When this task was last modified. Updated automatically on every change.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------- JPA Lifecycle Callbacks ----------

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
