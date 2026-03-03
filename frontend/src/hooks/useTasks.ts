import { useState, useCallback } from 'react';
import { Task, CreateTaskRequest, UpdateTaskRequest } from '../types/task';
import { apiService } from '../services/api';

/**
 * useTasks is a custom React hook that manages task-related state and API calls.
 * 
 * Custom hooks are a powerful pattern in React that lets us extract and reuse
 * stateful logic. Instead of duplicating API call code in every component,
 * we centralize it here.
 * 
 * This hook provides:
 * - tasks: array of current tasks
 * - loading: boolean indicating if an API call is in progress
 * - error: error message if something went wrong
 * - CRUD methods: fetchTasks, createTask, updateTask, deleteTask, etc.
 * 
 * Usage:
 * const { tasks, loading, error, fetchTasks } = useTasks();
 * 
 * The useCallback hook ensures our functions don't get recreated on every render,
 * which prevents unnecessary re-renders in child components.
 */
export const useTasks = () => {
    // State for storing the list of tasks
    const [tasks, setTasks] = useState<Task[]>([]);

    // Loading state - true when an API call is in progress
    const [loading, setLoading] = useState(false);

    // Error state - stores error message if an API call fails
    const [error, setError] = useState<string | null>(null);

    /**
     * Fetch tasks from the API with optional filters.
     * Updates the tasks state with the response data.
     */
    const fetchTasks = useCallback(async (params?: {
        status?: string;
        priority?: string;
        assigneeId?: number;
    }) => {
        setLoading(true);
        setError(null);

        try {
            const response = await apiService.getTasks(params);
            setTasks(response.content);
        } catch (err: unknown) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to fetch tasks';
            setError(errorMessage);
            throw err;
        } finally {
            // This runs whether the try block succeeds or fails
            setLoading(false);
        }
    }, []);

    /**
     * Create a new task.
     * On success, adds the new task to the local state immediately
     * for a responsive UI (optimistic update).
     */
    const createTask = useCallback(async (taskData: CreateTaskRequest) => {
        setLoading(true);
        setError(null);

        try {
            const newTask = await apiService.createTask(taskData);
            // Add the new task to the beginning of the list
            setTasks((prev) => [newTask, ...prev]);
            return newTask;
        } catch (err: unknown) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to create task';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    /**
     * Update an existing task.
     * On success, replaces the old task data in the local state.
     */
    const updateTask = useCallback(async (id: number, taskData: UpdateTaskRequest) => {
        setLoading(true);
        setError(null);

        try {
            const updatedTask = await apiService.updateTask(id, taskData);
            // Replace the old task with the updated one
            setTasks((prev) =>
                prev.map((task) => (task.id === id ? updatedTask : task))
            );
            return updatedTask;
        } catch (err: unknown) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to update task';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    /**
     * Delete a task.
     * On success, removes the task from the local state.
     */
    const deleteTask = useCallback(async (id: number) => {
        setLoading(true);
        setError(null);

        try {
            await apiService.deleteTask(id);
            // Remove the deleted task from the list
            setTasks((prev) => prev.filter((task) => task.id !== id));
        } catch (err: unknown) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to delete task';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    /**
     * Update only the status of a task.
     * Used primarily by the drag-and-drop feature.
     */
    const updateTaskStatus = useCallback(async (id: number, status: string) => {
        setError(null);

        try {
            const updatedTask = await apiService.updateTaskStatus(id, status);
            setTasks((prev) =>
                prev.map((task) => (task.id === id ? updatedTask : task))
            );
            return updatedTask;
        } catch (err: unknown) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to update task status';
            setError(errorMessage);
            throw err;
        }
    }, []);

    return {
        tasks,
        setTasks,
        loading,
        error,
        fetchTasks,
        createTask,
        updateTask,
        deleteTask,
        updateTaskStatus,
    };
};
