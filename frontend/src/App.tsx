import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuthContext } from './context/AuthContext';
import { TaskProvider } from './context/TaskContext';
import Layout from './components/Layout/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';

/**
 * App is the root component of our React application.
 * 
 * It sets up:
 * 1. React Router for client-side navigation
 * 2. AuthProvider for global authentication state
 * 3. TaskProvider for global task state
 * 4. Protected routes that require authentication
 * 5. Toast notifications for user feedback
 * 
 * The component hierarchy is:
 * App
 *  └── Router (handles URL routing)
 *       └── AuthProvider (provides auth state)
 *            └── Routes (defines page mappings)
 *                 ├── /login → Login page
 *                 ├── /register → Register page
 *                 └── / → Dashboard (protected)
 */
const App: React.FC = () => {
    return (
        <Router>
            <AuthProvider>
                <TaskProvider>
                    {/* Toast notifications container */}
                    <Toaster
                        position="top-right"
                        toastOptions={{
                            duration: 3000,
                            style: {
                                borderRadius: '10px',
                                background: '#333',
                                color: '#fff',
                            },
                        }}
                    />
                    <Routes>
                        {/* Public routes - accessible without login */}
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />

                        {/* Protected routes - require authentication */}
                        <Route
                            path="/"
                            element={
                                <ProtectedRoute>
                                    <Layout>
                                        <Dashboard />
                                    </Layout>
                                </ProtectedRoute>
                            }
                        />

                        {/* Catch-all: redirect unknown routes to home */}
                        <Route path="*" element={<Navigate to="/" replace />} />
                    </Routes>
                </TaskProvider>
            </AuthProvider>
        </Router>
    );
};

/**
 * ProtectedRoute component - redirects to login if not authenticated.
 * 
 * This is a common pattern for guarding routes that require authentication.
 * If the user is not logged in, they are redirected to /login.
 * If they are logged in, the child components are rendered normally.
 */
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { isAuthenticated } = useAuthContext();

    if (!isAuthenticated()) {
        // Redirect to login page
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
};

export default App;
