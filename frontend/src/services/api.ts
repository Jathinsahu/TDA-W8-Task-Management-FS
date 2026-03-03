import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { Task, CreateTaskRequest, UpdateTaskRequest, AuthResponse, PageResponse } from '../types/task';

/**
 * ApiService class handles all HTTP communication with the Spring Boot backend.
 * 
 * This service uses axios as the HTTP client and implements:
 * 1. Base URL configuration from environment variables
 * 2. Request interceptor: automatically adds JWT token to every request
 * 3. Response interceptor: handles token refresh on 401 errors
 * 
 * The interceptor pattern ensures we don't have to manually add tokens
 * to every API call. The axios instance handles it automatically.
 * 
 * Token flow:
 * - On login: store accessToken and refreshToken in localStorage
 * - On API call: interceptor adds accessToken to Authorization header
 * - On 401 error: interceptor tries to refresh the token automatically
 * - If refresh fails: redirect to login page
 */
class ApiService {
    private axiosInstance: AxiosInstance;
    private baseURL: string;

    constructor() {
        // Use environment variable or fallback to localhost
        this.baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

        // Create axios instance with default config
        this.axiosInstance = axios.create({
            baseURL: this.baseURL,
            timeout: 10000, // 10 second timeout
            headers: {
                'Content-Type': 'application/json',
            },
        });

        // ---- REQUEST INTERCEPTOR ----
        // Runs before every request to add the JWT token
        this.axiosInstance.interceptors.request.use(
            (config: InternalAxiosRequestConfig) => {
                const token = localStorage.getItem('accessToken');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            (error) => {
                return Promise.reject(error);
            }
        );

        // ---- RESPONSE INTERCEPTOR ----
        // Runs after every response to handle token refresh on 401 errors
        this.axiosInstance.interceptors.response.use(
            // Success handler - just pass through the response
            (response: AxiosResponse) => response,

            // Error handler - check if we need to refresh the token
            async (error) => {
                const originalRequest = error.config;

                // If we get a 401 and haven't tried refreshing yet
                if (error.response?.status === 401 && !originalRequest._retry) {
                    originalRequest._retry = true;

                    try {
                        const refreshToken = localStorage.getItem('refreshToken');
                        if (!refreshToken) {
                            throw new Error('No refresh token available');
                        }

                        // Try to get new tokens using the refresh token
                        const response = await axios.post(
                            `${this.baseURL}/auth/refresh`,
                            { refreshToken }
                        );

                        const { accessToken, refreshToken: newRefreshToken } = response.data;

                        // Store the new tokens
                        localStorage.setItem('accessToken', accessToken);
                        localStorage.setItem('refreshToken', newRefreshToken);

                        // Retry the original request with the new token
                        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                        return this.axiosInstance(originalRequest);
                    } catch (refreshError) {
                        // Refresh failed - clear tokens and redirect to login
                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        localStorage.removeItem('user');
                        window.location.href = '/login';
                        return Promise.reject(refreshError);
                    }
                }

                return Promise.reject(error);
            }
        );
    }

    // ==================== AUTH METHODS ====================

    /** Login with email and password */
    async login(email: string, password: string): Promise<AuthResponse> {
        const response = await this.axiosInstance.post('/auth/login', { email, password });
        return response.data;
    }

    /** Register a new account */
    async register(email: string, password: string, name: string): Promise<AuthResponse> {
        const response = await this.axiosInstance.post('/auth/register', { email, password, name });
        return response.data;
    }

    /** Logout - invalidate refresh token */
    async logout(): Promise<void> {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
            try {
                await this.axiosInstance.post('/auth/logout', { refreshToken });
            } catch (error) {
                // Ignore logout errors - we'll clear tokens anyway
                console.error('Logout API error:', error);
            }
        }
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
    }

    // ==================== TASK METHODS ====================

    /** Get tasks with optional filters and pagination */
    async getTasks(params?: {
        status?: string;
        priority?: string;
        assigneeId?: number;
        page?: number;
        size?: number;
        sort?: string;
    }): Promise<PageResponse<Task>> {
        const response = await this.axiosInstance.get('/tasks', { params });
        return response.data;
    }

    /** Get a single task by ID */
    async getTask(id: number): Promise<Task> {
        const response = await this.axiosInstance.get(`/tasks/${id}`);
        return response.data;
    }

    /** Create a new task */
    async createTask(task: CreateTaskRequest): Promise<Task> {
        const response = await this.axiosInstance.post('/tasks', task);
        return response.data;
    }

    /** Update an existing task */
    async updateTask(id: number, task: UpdateTaskRequest): Promise<Task> {
        const response = await this.axiosInstance.put(`/tasks/${id}`, task);
        return response.data;
    }

    /** Delete a task */
    async deleteTask(id: number): Promise<void> {
        await this.axiosInstance.delete(`/tasks/${id}`);
    }

    /** Update only the status of a task (used by drag-and-drop) */
    async updateTaskStatus(id: number, status: string): Promise<Task> {
        const response = await this.axiosInstance.put(`/tasks/${id}/status`, { status });
        return response.data;
    }

    /** Search tasks by keyword */
    async searchTasks(query: string, page: number = 0): Promise<PageResponse<Task>> {
        const response = await this.axiosInstance.get('/tasks/search', {
            params: { q: query, page, size: 20 },
        });
        return response.data;
    }
}

// Export a single instance - this is the Singleton pattern
// All components use the same ApiService instance
export const apiService = new ApiService();
