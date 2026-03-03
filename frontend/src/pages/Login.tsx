import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthContext } from '../context/AuthContext';
import { LogIn, Mail, Lock, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';

/**
 * Login page component.
 * 
 * Displays a login form with email and password fields.
 * On successful login, redirects to the dashboard.
 * On failure, shows an error message.
 * 
 * Features:
 * - Form validation (required fields)
 * - Loading state during API call
 * - Error display for invalid credentials
 * - Link to registration page
 * - Responsive design with Tailwind CSS
 * 
 * Demo credentials (seeded by DataInitializer):
 * - Admin: admin@taskmanager.com / admin123
 * - User: user@taskmanager.com / user123
 */
const Login: React.FC = () => {
    const navigate = useNavigate();
    const { login, loading } = useAuthContext();

    // Form state
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    /**
     * Handle form submission.
     * Calls the login function from AuthContext and handles the result.
     */
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault(); // Prevent page reload
        setError('');

        try {
            await login(email, password);
            toast.success('Welcome back!');
            navigate('/'); // Redirect to dashboard
        } catch (err) {
            setError('Invalid email or password. Please try again.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 via-white to-blue-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 px-4">
            <div className="w-full max-w-md">
                {/* Logo / App Name */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary-600 text-white mb-4 shadow-lg">
                        <LogIn size={28} />
                    </div>
                    <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
                        Task Manager
                    </h1>
                    <p className="text-gray-500 dark:text-gray-400 mt-2">
                        Sign in to manage your tasks
                    </p>
                </div>

                {/* Login Form Card */}
                <div className="card p-8">
                    {/* Error Alert */}
                    {error && (
                        <div className="flex items-center gap-2 p-3 mb-4 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg text-sm animate-slide-up">
                            <AlertCircle size={16} />
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-5">
                        {/* Email Field */}
                        <div>
                            <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                                Email Address
                            </label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                                <input
                                    id="email"
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="input-field pl-10"
                                    placeholder="you@example.com"
                                    required
                                />
                            </div>
                        </div>

                        {/* Password Field */}
                        <div>
                            <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                                Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                                <input
                                    id="password"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="input-field pl-10"
                                    placeholder="Enter your password"
                                    required
                                />
                            </div>
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="btn-primary w-full flex items-center justify-center gap-2 py-3"
                        >
                            {loading ? (
                                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                            ) : (
                                <>
                                    <LogIn size={18} />
                                    Sign In
                                </>
                            )}
                        </button>
                    </form>

                    {/* Demo Credentials Info */}
                    <div className="mt-4 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg text-sm text-blue-700 dark:text-blue-300">
                        <p className="font-medium mb-1">Demo Credentials:</p>
                        <p>Admin: admin@taskmanager.com / admin123</p>
                        <p>User: user@taskmanager.com / user123</p>
                    </div>

                    {/* Register Link */}
                    <p className="text-center text-sm text-gray-500 dark:text-gray-400 mt-6">
                        Don't have an account?{' '}
                        <Link to="/register" className="text-primary-600 hover:text-primary-700 font-medium">
                            Create Account
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Login;
