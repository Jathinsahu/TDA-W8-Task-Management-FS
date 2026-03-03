package com.taskmanager.model.dto;

import com.taskmanager.model.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * CreateTaskRequest is the DTO for creating a new task.
 * 
 * We use Jakarta Validation annotations (@NotBlank, @Size) to validate
 * the incoming request data. Spring Boot automatically validates these
 * when we use @Valid in the controller method parameter.
 * 
 * If validation fails, Spring returns a 400 Bad Request response
 * with details about which fields failed validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private TaskPriority priority;

    private LocalDate dueDate;

    private Long assigneeId;
}
