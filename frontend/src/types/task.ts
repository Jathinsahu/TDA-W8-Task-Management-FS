/**
 * TypeScript type definitions for the Task Management application.
 * 
 * These types define the shape of data flowing through our application.
 * TypeScript catches type errors at compile time, preventing runtime bugs
 * like accessing a property that doesn't exist on an object.
 * 
 * We define types for:
 * - Task: the main data model
 * - API request/response objects
 * - Enum-like types for status and priority
 */

// ==================== Enum Types ====================
// Using string literal unions instead of enums for better tree-shaking

/** The possible statuses a task can have, matching the backend enum */
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'COMPLETED';

/** Priority levels for tasks, matching the backend enum */
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

// ==================== Data Models ====================

/** Represents a task as returned by the API */
export interface Task {
    id: number;
    title: string;
    description: string | null;
    status: TaskStatus;
    priority: TaskPriority;
    dueDate: string | null;       // ISO date string (e.g., "2024-01-25")
    assigneeId: number | null;
    assigneeName: string | null;
    createdById: number | null;
    createdByName: string | null;
    createdAt: string;            // ISO datetime string
    updatedAt: string;            // ISO datetime string
}

// ==================== Request Types ====================

/** Request body for creating a new task */
export interface CreateTaskRequest {
    title: string;
    description?: string;
    priority?: TaskPriority;
    dueDate?: string;
    assigneeId?: number;
}

/** Request body for updating an existing task */
export interface UpdateTaskRequest {
    title?: string;
    description?: string;
    status?: TaskStatus;
    priority?: TaskPriority;
    dueDate?: string;
    assigneeId?: number;
}

// ==================== Auth Types ====================

/** Response from login/register endpoints */
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    userId: number;
    name: string;
    email: string;
    role: string;
}

/** Currently logged in user information */
export interface User {
    userId: number;
    name: string;
    email: string;
    role: string;
}

/** Login form data */
export interface LoginCredentials {
    email: string;
    password: string;
}

/** Registration form data */
export interface RegisterData {
    name: string;
    email: string;
    password: string;
}

// ==================== Pagination Types ====================

/** Spring Boot Page response format */
export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;       // Current page number (0-based)
    first: boolean;
    last: boolean;
}

// ==================== WebSocket Types ====================

/** WebSocket message format */
export interface WebSocketMessage {
    type: 'TASK_CREATED' | 'TASK_UPDATED' | 'TASK_DELETED';
    payload: Task | number;   // Task object or task ID (for delete)
}
