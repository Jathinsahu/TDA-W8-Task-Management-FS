package com.taskmanager.service;

import com.taskmanager.model.dto.CreateTaskRequest;
import com.taskmanager.model.dto.TaskDto;
import com.taskmanager.model.dto.UpdateTaskRequest;
import com.taskmanager.model.entity.Task;
import com.taskmanager.model.entity.User;
import com.taskmanager.model.enums.TaskPriority;
import com.taskmanager.model.enums.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TaskService contains all the business logic for task management.
 * 
 * This service acts as the middle layer between the controller (which handles
 * HTTP requests) and the repository (which handles database operations).
 * 
 * Key responsibilities:
 * 1. CRUD operations on tasks
 * 2. Converting between entities and DTOs
 * 3. Applying business rules (e.g., only the creator can delete a task)
 * 4. Filtering and searching tasks
 * 5. Notifying connected clients of changes via WebSocket
 * 
 * We use DTOs (Data Transfer Objects) to:
 * - Control what data is exposed in API responses
 * - Avoid circular reference issues with JPA entities
 * - Decouple the API contract from the database schema
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;

    /**
     * Get tasks with optional filtering and pagination.
     * 
     * Supports filtering by:
     * - status: filter by task status (TODO, IN_PROGRESS, REVIEW, COMPLETED)
     * - priority: filter by priority level (LOW, MEDIUM, HIGH, URGENT)
     * - assigneeId: filter by assigned user
     * 
     * Results are paginated to avoid loading all tasks at once.
     * Default is 20 tasks per page, sorted by creation date (newest first).
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(String status, String priority, Long assigneeId, 
                                  Pageable pageable, User currentUser) {
        logger.debug("Fetching tasks with filters - status: {}, priority: {}, assigneeId: {}", 
                     status, priority, assigneeId);

        // Parse enum values from strings (null if not provided)
        TaskStatus taskStatus = status != null ? TaskStatus.valueOf(status) : null;
        TaskPriority taskPriority = priority != null ? TaskPriority.valueOf(priority) : null;

        // Use the custom query that handles optional filters
        Page<Task> tasks = taskRepository.findWithFilters(taskStatus, taskPriority, assigneeId, pageable);

        // Convert each Task entity to a TaskDto
        // The .map() method transforms each element while preserving pagination info
        return tasks.map(this::convertToDto);
    }

    /**
     * Get a single task by its ID.
     * 
     * @throws RuntimeException if the task doesn't exist
     */
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return convertToDto(task);
    }

    /**
     * Create a new task.
     * 
     * The task is created with:
     * - Status: TODO (default for new tasks)
     * - Priority: from request, defaults to MEDIUM
     * - CreatedBy: the currently logged-in user
     * - Assignee: from request (optional, can be assigned later)
     * 
     * After creation, a WebSocket notification is sent to update all
     * connected clients in real-time.
     */
    @Transactional
    public TaskDto createTask(CreateTaskRequest request, User currentUser) {
        logger.info("Creating new task: '{}' by user: {}", request.getTitle(), currentUser.getEmail());

        // Build the task entity from the request
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .createdBy(currentUser)
                .build();

        // If an assignee ID was provided, look up the user and set them
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        // Save to database
        task = taskRepository.save(task);
        logger.info("Task created with id: {}", task.getId());

        TaskDto taskDto = convertToDto(task);

        // Notify all connected clients about the new task
        webSocketService.notifyTaskCreated(taskDto);

        return taskDto;
    }

    /**
     * Update an existing task.
     * 
     * Only updates fields that are not null in the request,
     * allowing partial updates (you can update just the title
     * without changing other fields).
     */
    @Transactional
    public TaskDto updateTask(Long id, UpdateTaskRequest request, User currentUser) {
        logger.info("Updating task id: {} by user: {}", id, currentUser.getEmail());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        // Only update fields that were provided in the request
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }

        // Save updated task
        task = taskRepository.save(task);
        logger.info("Task updated: {}", task.getId());

        TaskDto taskDto = convertToDto(task);
        webSocketService.notifyTaskUpdated(taskDto);

        return taskDto;
    }

    /**
     * Update only the status of a task.
     * Used by the drag-and-drop feature on the Kanban board.
     */
    @Transactional
    public TaskDto updateTaskStatus(Long id, String status, User currentUser) {
        logger.info("Updating task status - id: {}, new status: {}", id, status);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setStatus(TaskStatus.valueOf(status));
        task = taskRepository.save(task);

        TaskDto taskDto = convertToDto(task);
        webSocketService.notifyTaskUpdated(taskDto);

        return taskDto;
    }

    /**
     * Delete a task by its ID.
     * Sends a WebSocket notification so all clients remove the task
     * from their Kanban boards.
     */
    @Transactional
    public void deleteTask(Long id, User currentUser) {
        logger.info("Deleting task id: {} by user: {}", id, currentUser.getEmail());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        taskRepository.delete(task);
        webSocketService.notifyTaskDeleted(id);

        logger.info("Task deleted: {}", id);
    }

    /**
     * Search tasks by keyword in title or description.
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> searchTasks(String searchTerm, Pageable pageable) {
        return taskRepository.searchTasks(searchTerm, pageable).map(this::convertToDto);
    }

    /**
     * Convert a Task entity to a TaskDto.
     * 
     * This method extracts the needed fields from related entities
     * (User) and creates a flat DTO object. This avoids:
     * 1. Sending unnecessary data to the client
     * 2. Circular references (user -> tasks -> user -> tasks...)
     * 3. Lazy loading issues with JPA
     */
    private TaskDto convertToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null)
                .createdById(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .createdByName(task.getCreatedBy() != null ? task.getCreatedBy().getName() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
