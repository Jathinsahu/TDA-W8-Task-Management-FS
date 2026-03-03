package com.taskmanager.model.enums;

/**
 * TaskStatus represents the different stages a task can be in.
 * 
 * We use an enum here because the status values are fixed and known ahead of time.
 * This is much safer than using plain strings because the compiler will catch
 * any typos or invalid values at compile time rather than at runtime.
 * 
 * The flow of a task through statuses is typically:
 * TODO -> IN_PROGRESS -> REVIEW -> COMPLETED
 * But tasks can also be moved back to previous statuses if needed.
 */
public enum TaskStatus {
    TODO,           // Task has been created but work hasn't started
    IN_PROGRESS,    // Someone is actively working on this task
    REVIEW,         // Task is done and waiting for review/approval
    COMPLETED       // Task is finished and approved
}
