import React, { createContext, useContext, useEffect, ReactNode } from 'react';
import { useAuth } from '../hooks/useAuth';
import { webSocketService } from '../services/websocket';
import { User } from '../types/task';

/**
 * AuthContext provides authentication state to the entire application.
 * 
 * React Context is used to avoid "prop drilling" - passing authentication
 * data through every component in the tree. Instead, any component can
 * access the auth state directly using useAuthContext().
 * 
 * The context pattern works as follows:
 * 1. AuthProvider wraps the app at a high level (in App.tsx)
 * 2. It maintains the auth state (user, tokens, loading)
 * 3. Child components can access this state via useAuthContext()
 * 4. When auth state changes, all consumers re-render
 * 
 * We also connect/disconnect WebSocket when auth state changes:
 * - User logs in → connect WebSocket for real-time updates
 * - User logs out → disconnect WebSocket
 */

// Define the shape of the context value
interface AuthContextType {
    user: User | null;
    loading: boolean;
    error: string | null;
    isAuthenticated: () => boolean;
    login: (email: string, password: string) => Promise<void>;
    register: (name: string, email: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
}

// Create the context with a default value of null
// We'll check for null in useAuthContext to ensure the provider exists
const AuthContext = createContext<AuthContextType | null>(null);

/**
 * AuthProvider component - wraps children with authentication context.
 * Place this high in the component tree (usually in App.tsx).
 */
export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const auth = useAuth();

    // Connect/disconnect WebSocket based on authentication status
    useEffect(() => {
        if (auth.isAuthenticated()) {
            webSocketService.connect();
        }

        // Cleanup: disconnect on unmount or when auth changes
        return () => {
            webSocketService.disconnect();
        };
    }, [auth.user]); // Re-run when user changes (login/logout)

    return (
        <AuthContext.Provider value={auth as AuthContextType}>
            {children}
        </AuthContext.Provider>
    );
};

/**
 * Custom hook to access authentication context.
 * 
 * Throws an error if used outside of AuthProvider to catch
 * programming mistakes early.
 * 
 * Usage:
 * const { user, login, logout } = useAuthContext();
 */
export const useAuthContext = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuthContext must be used within an AuthProvider');
    }
    return context;
};
