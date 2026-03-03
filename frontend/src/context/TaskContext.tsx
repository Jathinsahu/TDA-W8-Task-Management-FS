import React, { createContext, useContext, useEffect, ReactNode, useCallback } from 'react';
import { useTasks } from '../hooks/useTasks';
import { webSocketService } from '../services/websocket';
import { Task, CreateTaskRequest, UpdateTaskRequest, WebSocketMessage } from '../types/task';
import { useAuthContext } from './AuthContext';

/**
 * TaskContext provides task state and operations to the entire application.
 * 
 * This context manages:
 * 1. Loading tasks from the API when the user is authenticated
 * 2. CRUD operations on tasks
 * 3. Real-time updates from WebSocket
 * 
 * WebSocket integration is the key feature here. When another user
 * creates/updates/deletes a task, the WebSocket message triggers
 * a state update that immediately reflects on everyone's Kanban board.
 */

interface TaskContextType {
    tasks: Task[];
    loading: boolean;
    error: string | null;
    fetchTasks: (params?: { status?: string; priority?: string }) => Promise<void>;
    createTask: (data: CreateTaskRequest) => Promise<Task>;
    updateTask: (id: number, data: UpdateTaskRequest) => Promise<Task>;
    deleteTask: (id: number) => Promise<void>;
    updateTaskStatus: (id: number, status: string) => Promise<Task>;
}

const TaskContext = createContext<TaskContextType | null>(null);

export const TaskProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const taskHook = useTasks();
    const { user } = useAuthContext();

    // Fetch tasks when the user is authenticated
    useEffect(() => {
        if (user) {
            taskHook.fetchTasks();
        }
    }, [user]);

    // Handle WebSocket messages for real-time updates
    const handleWebSocketMessage = useCallback((message: WebSocketMessage) => {
        switch (message.type) {
            case 'TASK_CREATED': {
                // Add the new task if it doesn't already exist
                const newTask = message.payload as Task;
                taskHook.setTasks((prev) => {
                    const exists = prev.find((t) => t.id === newTask.id);
                    if (exists) return prev;
                    return [newTask, ...prev];
                });
                break;
            }
            case 'TASK_UPDATED': {
                // Update the existing task in the list
                const updatedTask = message.payload as Task;
                taskHook.setTasks((prev) =>
                    prev.map((t) => (t.id === updatedTask.id ? updatedTask : t))
                );
                break;
            }
            case 'TASK_DELETED': {
                // Remove the deleted task from the list
                const taskId = message.payload as number;
                taskHook.setTasks((prev) => prev.filter((t) => t.id !== taskId));
                break;
            }
        }
    }, []);

    // Subscribe to WebSocket messages
    useEffect(() => {
        if (user) {
            const unsubscribe = webSocketService.onMessage(handleWebSocketMessage);
            return unsubscribe;
        }
    }, [user, handleWebSocketMessage]);

    return (
        <TaskContext.Provider value={taskHook as TaskContextType}>
            {children}
        </TaskContext.Provider>
    );
};

/**
 * Hook to access task context.
 * Usage: const { tasks, createTask, updateTask } = useTaskContext();
 */
export const useTaskContext = (): TaskContextType => {
    const context = useContext(TaskContext);
    if (!context) {
        throw new Error('useTaskContext must be used within a TaskProvider');
    }
    return context;
};
