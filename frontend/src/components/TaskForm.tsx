import React, { useState } from 'react';
import { CreateTaskRequest, TaskPriority } from '../types/task';
import { X, Plus, Calendar } from 'lucide-react';

/**
 * TaskForm component for creating new tasks.
 * 
 * This is a modal form that appears when the user clicks "Add Task".
 * It collects task details (title, description, priority, due date)
 * and calls the onSubmit callback with the form data.
 * 
 * The modal uses a backdrop overlay for focus and can be closed
 * by clicking the X button or the overlay.
 */
interface TaskFormProps {
    onSubmit: (data: CreateTaskRequest) => Promise<void>;
    onClose: () => void;
}

const TaskForm: React.FC<TaskFormProps> = ({ onSubmit, onClose }) => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [priority, setPriority] = useState<TaskPriority>('MEDIUM');
    const [dueDate, setDueDate] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!title.trim()) return;

        setSubmitting(true);
        try {
            await onSubmit({
                title: title.trim(),
                description: description.trim() || undefined,
                priority,
                dueDate: dueDate || undefined,
            });
            onClose(); // Close the modal on success
        } catch (error) {
            console.error('Failed to create task:', error);
        } finally {
            setSubmitting(false);
        }
    };

    // Priority options with colors for the select dropdown
    const priorities: { value: TaskPriority; label: string; color: string }[] = [
        { value: 'LOW', label: 'Low', color: 'text-emerald-600' },
        { value: 'MEDIUM', label: 'Medium', color: 'text-amber-600' },
        { value: 'HIGH', label: 'High', color: 'text-orange-600' },
        { value: 'URGENT', label: 'Urgent', color: 'text-red-600' },
    ];

    return (
        // Modal overlay - clicking it closes the form
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
            onClick={(e) => {
                if (e.target === e.currentTarget) onClose();
            }}
        >
            {/* Modal content */}
            <div className="w-full max-w-lg card p-6 animate-slide-up">
                {/* Header */}
                <div className="flex items-center justify-between mb-6">
                    <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
                        Create New Task
                    </h2>
                    <button
                        onClick={onClose}
                        className="p-1 rounded-lg text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="space-y-4">
                    {/* Title */}
                    <div>
                        <label htmlFor="task-title" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                            Task Title *
                        </label>
                        <input
                            id="task-title"
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            className="input-field"
                            placeholder="What needs to be done?"
                            required
                            autoFocus
                        />
                    </div>

                    {/* Description */}
                    <div>
                        <label htmlFor="task-desc" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                            Description
                        </label>
                        <textarea
                            id="task-desc"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            className="input-field resize-none"
                            rows={3}
                            placeholder="Add a more detailed description..."
                        />
                    </div>

                    {/* Priority and Due Date row */}
                    <div className="grid grid-cols-2 gap-4">
                        {/* Priority */}
                        <div>
                            <label htmlFor="task-priority" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                                Priority
                            </label>
                            <select
                                id="task-priority"
                                value={priority}
                                onChange={(e) => setPriority(e.target.value as TaskPriority)}
                                className="input-field"
                            >
                                {priorities.map((p) => (
                                    <option key={p.value} value={p.value}>
                                        {p.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {/* Due Date */}
                        <div>
                            <label htmlFor="task-due" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                                Due Date
                            </label>
                            <div className="relative">
                                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                                <input
                                    id="task-due"
                                    type="date"
                                    value={dueDate}
                                    onChange={(e) => setDueDate(e.target.value)}
                                    className="input-field pl-10"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
                        <button
                            type="button"
                            onClick={onClose}
                            className="btn-secondary"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={submitting || !title.trim()}
                            className="btn-primary flex items-center gap-2"
                        >
                            {submitting ? (
                                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                            ) : (
                                <Plus size={18} />
                            )}
                            Create Task
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default TaskForm;
