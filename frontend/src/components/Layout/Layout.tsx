import React, { useState } from 'react';
import { useAuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Menu, X, Sun, Moon, LogOut, User } from 'lucide-react';

/**
 * Layout component wraps the main content with a navigation bar.
 * 
 * Features:
 * - App name and navigation
 * - User info display
 * - Dark/light mode toggle
 * - Logout button
 * - Responsive hamburger menu for mobile
 */
interface LayoutProps {
    children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
    const { user, logout } = useAuthContext();
    const navigate = useNavigate();

    // State for dark mode (persisted in localStorage)
    const [isDark, setIsDark] = useState(() => {
        const saved = localStorage.getItem('darkMode');
        if (saved === 'true') {
            document.documentElement.classList.add('dark');
            return true;
        }
        return false;
    });

    // State for mobile menu
    const [menuOpen, setMenuOpen] = useState(false);

    /**
     * Toggle dark mode.
     * Adds/removes the "dark" class on the html element,
     * which Tailwind uses for dark mode variants.
     */
    const toggleDarkMode = () => {
        const newMode = !isDark;
        setIsDark(newMode);
        localStorage.setItem('darkMode', String(newMode));
        document.documentElement.classList.toggle('dark', newMode);
    };

    /**
     * Handle logout - clear auth state and redirect to login.
     */
    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-200">
            {/* Navigation Bar */}
            <nav className="sticky top-0 z-50 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 shadow-sm">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center justify-between h-16">
                        {/* Left side - App name */}
                        <div className="flex items-center gap-3">
                            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
                                <span className="text-white font-bold text-sm">TM</span>
                            </div>
                            <h1 className="text-xl font-bold text-gray-900 dark:text-white">
                                Task Manager
                            </h1>
                        </div>

                        {/* Right side - Desktop */}
                        <div className="hidden md:flex items-center gap-4">
                            {/* Dark mode toggle */}
                            <button
                                onClick={toggleDarkMode}
                                className="p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                                aria-label="Toggle dark mode"
                            >
                                {isDark ? <Sun size={20} /> : <Moon size={20} />}
                            </button>

                            {/* User info */}
                            <div className="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
                                <div className="w-8 h-8 bg-primary-100 dark:bg-primary-900 rounded-full flex items-center justify-center">
                                    <User size={16} className="text-primary-600 dark:text-primary-400" />
                                </div>
                                <span className="font-medium">{user?.name}</span>
                            </div>

                            {/* Logout button */}
                            <button
                                onClick={handleLogout}
                                className="flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                            >
                                <LogOut size={16} />
                                Logout
                            </button>
                        </div>

                        {/* Mobile menu button */}
                        <button
                            onClick={() => setMenuOpen(!menuOpen)}
                            className="md:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700"
                        >
                            {menuOpen ? <X size={24} /> : <Menu size={24} />}
                        </button>
                    </div>

                    {/* Mobile Menu */}
                    {menuOpen && (
                        <div className="md:hidden py-4 space-y-2 border-t border-gray-200 dark:border-gray-700 animate-slide-up">
                            <div className="flex items-center gap-2 px-3 py-2 text-sm text-gray-700 dark:text-gray-300">
                                <User size={16} />
                                <span>{user?.name} ({user?.email})</span>
                            </div>
                            <button
                                onClick={toggleDarkMode}
                                className="w-full flex items-center gap-2 px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
                            >
                                {isDark ? <Sun size={16} /> : <Moon size={16} />}
                                {isDark ? 'Light Mode' : 'Dark Mode'}
                            </button>
                            <button
                                onClick={handleLogout}
                                className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg"
                            >
                                <LogOut size={16} />
                                Logout
                            </button>
                        </div>
                    )}
                </div>
            </nav>

            {/* Main Content */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                {children}
            </main>
        </div>
    );
};

export default Layout;
