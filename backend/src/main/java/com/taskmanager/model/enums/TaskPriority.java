package com.taskmanager.model.enums;

/**
 * TaskPriority defines how urgent or important a task is.
 * 
 * Priority levels help users organize and focus on what matters most.
 * In the frontend, each priority level is displayed with a different color:
 * - LOW: green (not urgent)
 * - MEDIUM: yellow/orange (normal priority)
 * - HIGH: red (needs attention soon)
 * - URGENT: dark red with animation (needs immediate attention)
 */
public enum TaskPriority {
    LOW,        // Can be done when there's free time
    MEDIUM,     // Should be done in the normal workflow
    HIGH,       // Important, should be prioritized
    URGENT      // Critical, needs immediate attention
}
