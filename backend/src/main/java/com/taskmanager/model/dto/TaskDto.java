package com.taskmanager.model.dto;

import com.taskmanager.model.enums.TaskPriority;
import com.taskmanager.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TaskDto is a Data Transfer Object for sending task data to the frontend.
 * 
 * We use DTOs instead of sending entity objects directly because:
 * 1. We can control exactly what data the client receives (security)
 * 2. We avoid circular references (user has tasks, task has user)
 * 3. We can shape the data differently than how it's stored in the database
 * 4. Changes to the entity don't automatically change the API response
 * 
 * This DTO includes flattened user information (assigneeId, assigneeName)
 * instead of nested User objects, which makes the JSON response simpler.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private Long assigneeId;
    private String assigneeName;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
