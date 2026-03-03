import { useState, useCallback } from 'react';
import { apiService } from '../services/api';
import { User, AuthResponse } from '../types/task';

/**
 * useAuth is a custom React hook for authentication-related state and logic.
 * 
 * It manages:
 * - User login/logout/registration
 * - Storing and retrieving auth state from localStorage
 * - Loading states during auth operations
 * 
 * localStorage is used to persist the auth state (tokens and user info)
 * across page refreshes. When the app loads, it checks localStorage for
 * existing tokens to automatically restore the user's session.
 */
export const useAuth = () => {
    // Initialize user from localStorage if available
    const [user, setUser] = useState<User | null>(() => {
        const stored = localStorage.getItem('user');
        return stored ? JSON.parse(stored) : null;
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    /**
     * Check if the user is currently authenticated.
     * We check for the access token because it's required for API calls.
     */
    const isAuthenticated = useCallback((): boolean => {
        return !!localStorage.getItem('accessToken');
    }, []);

    /**
     * Handle a successful auth response (from login or register).
     * Stores tokens and user info in localStorage and updates state.
     */
    const handleAuthSuccess = useCallback((response: AuthResponse) => {
        // Store tokens
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);

        // Store user info
        const userData: User = {
            userId: response.userId,
            name: response.name,
            email: response.email,
            role: response.role,
        };
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
    }, []);

    /**
     * Log in with email and password.
     */
    const login = useCallback(async (email: string, password: string) => {
        setLoading(true);
        setError(null);

        try {
            const response = await apiService.login(email, password);
            handleAuthSuccess(response);
            return response;
        } catch (err: unknown) {
            const errorMessage = err instanceof Error ? err.message : 'Login failed. Please check your credentials.';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [handleAuthSuccess]);

    /**
     * Register a new account.
     */
    const register = useCallback(async (name: string, email: string, password: string) => {
        setLoading(true);
        setError(null);

        try {
            const response = await apiService.register(email, password, name);
            handleAuthSuccess(response);
            return response;
        } catch (err: unknown) {
            const errorMessage = err instanceof Error ? err.message : 'Registration failed. Please try again.';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [handleAuthSuccess]);

    /**
     * Log out the current user.
     * Clears all tokens and user data.
     */
    const logout = useCallback(async () => {
        try {
            await apiService.logout();
        } catch (error) {
            console.error('Logout error:', error);
        }
        setUser(null);
    }, []);

    return {
        user,
        loading,
        error,
        isAuthenticated,
        login,
        register,
        logout,
    };
};
