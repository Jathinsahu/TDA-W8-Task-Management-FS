package com.taskmanager.repository;

import com.taskmanager.model.entity.Task;
import com.taskmanager.model.enums.TaskPriority;
import com.taskmanager.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * TaskRepository provides database operations for the Task entity.
 * 
 * This repository demonstrates several Spring Data JPA features:
 * 1. Derived queries (findByStatus, findByAssigneeId)
 * 2. Custom JPQL queries (@Query annotation)
 * 3. Pagination support (Pageable parameter, Page return type)
 * 
 * Pagination is important for performance because we might have
 * hundreds or thousands of tasks. Instead of loading all tasks at once,
 * we load them in pages (e.g., 20 at a time).
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find tasks by status with pagination.
     * Used to load tasks for a specific Kanban column.
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Find tasks assigned to a specific user with pagination.
     */
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    /**
     * Custom JPQL query for filtering tasks with multiple optional criteria.
     * 
     * This query uses conditional logic: a filter is only applied if its
     * parameter is not null. This allows the same query to handle different
     * combinations of filters without needing separate methods.
     * 
     * For example:
     * - If status is null and priority is "HIGH", it returns all HIGH priority tasks
     * - If status is "TODO" and priority is null, it returns all TODO tasks
     * - If both are provided, it returns tasks matching both criteria
     * 
     * @param status optional status filter
     * @param priority optional priority filter
     * @param assigneeId optional assignee filter
     * @param pageable pagination parameters (page number, page size, sort)
     * @return paginated list of matching tasks
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId)")
    Page<Task> findWithFilters(
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("assigneeId") Long assigneeId,
            Pageable pageable
    );

    /**
     * Search tasks by title or description (case-insensitive).
     * Used by the search feature in the frontend.
     * LOWER() function makes the search case-insensitive.
     */
    @Query("SELECT t FROM Task t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Task> searchTasks(@Param("searchTerm") String searchTerm, Pageable pageable);
}
