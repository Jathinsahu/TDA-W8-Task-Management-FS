package com.taskmanager.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO for updating only the status of a task.
 * Used by the drag-and-drop feature on the Kanban board,
 * where the user drags a task card from one column to another.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {
    private String status;
}
