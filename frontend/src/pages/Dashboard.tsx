import React, { useState } from 'react';
import { useTaskContext } from '../context/TaskContext';
import { useAuthContext } from '../context/AuthContext';
import TaskList from '../components/TaskList';
import TaskForm from '../components/TaskForm';
import { Plus, Search, LayoutGrid, List, RefreshCw, CheckCircle, Clock, AlertCircle, BarChart3 } from 'lucide-react';
import toast from 'react-hot-toast';

/**
 * Dashboard page - the main view of the application.
 * 
 * This page shows:
 * 1. Statistics cards (total tasks, in progress, completed, overdue)
 * 2. Search and filter controls
 * 3. Kanban board with drag-and-drop task cards
 * 4. "Add Task" button to create new tasks
 * 
 * The dashboard integrates with TaskContext for data and operations,
 * and all changes are reflected in real-time via WebSocket.
 */
const Dashboard: React.FC = () => {
    const { tasks, loading, error, fetchTasks, createTask, updateTaskStatus, deleteTask } = useTaskContext();
    const { user } = useAuthContext();

    // UI state
    const [showTaskForm, setShowTaskForm] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');

    // Filter tasks based on search query
    const filteredTasks = tasks.filter((task) => {
        if (!searchQuery) return true;
        const query = searchQuery.toLowerCase();
        return (
            task.title.toLowerCase().includes(query) ||
            (task.description && task.description.toLowerCase().includes(query))
        );
    });

    // Calculate statistics
    const stats = {
        total: tasks.length,
        todo: tasks.filter((t) => t.status === 'TODO').length,
        inProgress: tasks.filter((t) => t.status === 'IN_PROGRESS').length,
        review: tasks.filter((t) => t.status === 'REVIEW').length,
        completed: tasks.filter((t) => t.status === 'COMPLETED').length,
        overdue: tasks.filter((t) => {
            if (!t.dueDate || t.status === 'COMPLETED') return false;
            return new Date(t.dueDate) < new Date(new Date().toDateString());
        }).length,
    };

    /**
     * Handle creating a new task.
     */
    const handleCreateTask = async (data: any) => {
        try {
            await createTask(data);
            toast.success('Task created successfully!');
        } catch (error) {
            toast.error('Failed to create task');
            throw error;
        }
    };

    /**
     * Handle changing task status (from drag-and-drop).
     */
    const handleStatusChange = async (taskId: number, newStatus: string) => {
        await updateTaskStatus(taskId, newStatus);
    };

    /**
     * Handle deleting a task.
     */
    const handleDeleteTask = async (taskId: number) => {
        try {
            await deleteTask(taskId);
            toast.success('Task deleted');
        } catch (error) {
            toast.error('Failed to delete task');
        }
    };

    return (
        <div className="space-y-6">
            {/* Welcome Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                        Welcome back, {user?.name?.split(' ')[0]}! 👋
                    </h2>
                    <p className="text-gray-500 dark:text-gray-400 mt-1">
                        Here's an overview of your tasks
                    </p>
                </div>

                {/* Action buttons */}
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => fetchTasks()}
                        className="btn-secondary flex items-center gap-2 text-sm"
                        disabled={loading}
                    >
                        <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
                        Refresh
                    </button>
                    <button
                        onClick={() => setShowTaskForm(true)}
                        className="btn-primary flex items-center gap-2"
                    >
                        <Plus size={18} />
                        Add Task
                    </button>
                </div>
            </div>

            {/* Statistics Cards */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <StatCard
                    title="Total Tasks"
                    value={stats.total}
                    icon={<BarChart3 size={20} />}
                    color="text-primary-600"
                    bgColor="bg-primary-50 dark:bg-primary-900/20"
                />
                <StatCard
                    title="In Progress"
                    value={stats.inProgress}
                    icon={<Clock size={20} />}
                    color="text-blue-600"
                    bgColor="bg-blue-50 dark:bg-blue-900/20"
                />
                <StatCard
                    title="Completed"
                    value={stats.completed}
                    icon={<CheckCircle size={20} />}
                    color="text-green-600"
                    bgColor="bg-green-50 dark:bg-green-900/20"
                />
                <StatCard
                    title="Overdue"
                    value={stats.overdue}
                    icon={<AlertCircle size={20} />}
                    color="text-red-600"
                    bgColor="bg-red-50 dark:bg-red-900/20"
                />
            </div>

            {/* Search Bar */}
            <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search tasks by title or description..."
                    className="input-field pl-10"
                />
            </div>

            {/* Error State */}
            {error && (
                <div className="p-4 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg flex items-center gap-2">
                    <AlertCircle size={18} />
                    {error}
                    <button
                        onClick={() => fetchTasks()}
                        className="ml-auto text-sm underline hover:no-underline"
                    >
                        Retry
                    </button>
                </div>
            )}

            {/* Loading State */}
            {loading && tasks.length === 0 && (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    {[1, 2, 3, 4].map((i) => (
                        <div key={i} className="rounded-xl p-3 bg-gray-100 dark:bg-gray-800 min-h-[400px]">
                            <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-24 mb-3 animate-pulse-soft" />
                            <div className="space-y-2">
                                {[1, 2, 3].map((j) => (
                                    <div key={j} className="card p-3">
                                        <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-2 animate-pulse-soft" />
                                        <div className="h-3 bg-gray-200 dark:bg-gray-700 rounded w-1/2 animate-pulse-soft" />
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Kanban Board */}
            {!loading || tasks.length > 0 ? (
                <TaskList
                    tasks={filteredTasks}
                    onStatusChange={handleStatusChange}
                    onDeleteTask={handleDeleteTask}
                />
            ) : null}

            {/* Task Form Modal */}
            {showTaskForm && (
                <TaskForm
                    onSubmit={handleCreateTask}
                    onClose={() => setShowTaskForm(false)}
                />
            )}
        </div>
    );
};

/**
 * StatCard - reusable component for displaying a statistic.
 */
interface StatCardProps {
    title: string;
    value: number;
    icon: React.ReactNode;
    color: string;
    bgColor: string;
}

const StatCard: React.FC<StatCardProps> = ({ title, value, icon, color, bgColor }) => (
    <div className="card p-4 flex items-center gap-4">
        <div className={`p-3 rounded-xl ${bgColor}`}>
            <div className={color}>{icon}</div>
        </div>
        <div>
            <p className="text-2xl font-bold text-gray-900 dark:text-white">{value}</p>
            <p className="text-xs text-gray-500 dark:text-gray-400">{title}</p>
        </div>
    </div>
);

export default Dashboard;
