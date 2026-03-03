package com.taskmanager.controller;

import com.taskmanager.model.dto.CreateTaskRequest;
import com.taskmanager.model.dto.TaskDto;
import com.taskmanager.model.dto.UpdateTaskRequest;
import com.taskmanager.model.dto.UpdateTaskStatusRequest;
import com.taskmanager.model.entity.User;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * TaskController handles HTTP requests for task management operations.
 * 
 * This controller exposes REST endpoints for full CRUD operations on tasks:
 * - GET    /api/tasks        - List tasks with filtering and pagination
 * - GET    /api/tasks/{id}   - Get a specific task
 * - POST   /api/tasks        - Create a new task
 * - PUT    /api/tasks/{id}   - Update a task
 * - DELETE /api/tasks/{id}   - Delete a task
 * - PUT    /api/tasks/{id}/status - Update task status (for drag-and-drop)
 * - GET    /api/tasks/search - Search tasks by keyword
 * 
 * All endpoints here require authentication (configured in SecurityConfig).
 * The @AuthenticationPrincipal annotation automatically injects the currently
 * authenticated user from the SecurityContext.
 * 
 * We use @PageableDefault to set default pagination parameters:
 * - size: 20 tasks per page
 * - sort: by createdAt descending (newest first)
 * 
 * The Spring Data Pageable interface handles pagination parameters from
 * the query string (?page=0&size=20&sort=createdAt,desc).
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Get a paginated list of tasks with optional filters.
     * 
     * Query parameters:
     * - status: "TODO", "IN_PROGRESS", "REVIEW", "COMPLETED"
     * - priority: "LOW", "MEDIUM", "HIGH", "URGENT"
     * - assigneeId: numeric user ID
     * - page: page number (0-based)
     * - size: items per page
     * - sort: field,direction (e.g., "createdAt,desc")
     * 
     * Example: GET /api/tasks?status=TODO&priority=HIGH&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<TaskDto>> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {

        Page<TaskDto> tasks = taskService.getTasks(status, priority, assigneeId, pageable, currentUser);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get a single task by ID.
     * 
     * Example: GET /api/tasks/123
     * Returns 404 if the task doesn't exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /**
     * Create a new task.
     * 
     * Request body must include at least a title.
     * Returns 201 CREATED with the created task data.
     * 
     * Example request body:
     * {
     *   "title": "Fix login bug",
     *   "description": "Users cannot log in with special characters in password",
     *   "priority": "HIGH",
     *   "dueDate": "2024-02-15",
     *   "assigneeId": 2
     * }
     */
    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal User currentUser) {

        TaskDto createdTask = taskService.createTask(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /**
     * Update an existing task.
     * 
     * Only the fields included in the request body will be updated.
     * Fields that are null/missing will remain unchanged (partial update).
     * 
     * Example: PUT /api/tasks/123
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal User currentUser) {

        TaskDto updatedTask = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Delete a task.
     * 
     * Returns 204 NO CONTENT on success (no response body needed).
     * 
     * Example: DELETE /api/tasks/123
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update only the status of a task.
     * 
     * This endpoint is specifically for the drag-and-drop feature
     * on the Kanban board, where the user drags a task card from
     * one column (e.g., TODO) to another (e.g., IN_PROGRESS).
     * 
     * Example: PUT /api/tasks/123/status
     * Body: { "status": "IN_PROGRESS" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal User currentUser) {

        TaskDto updatedTask = taskService.updateTaskStatus(id, request.getStatus(), currentUser);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Search tasks by keyword.
     * 
     * Searches in both title and description fields (case-insensitive).
     * 
     * Example: GET /api/tasks/search?q=login&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TaskDto>> searchTasks(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<TaskDto> tasks = taskService.searchTasks(q, pageable);
        return ResponseEntity.ok(tasks);
    }
}
