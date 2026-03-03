import React from 'react';
import { Task, TaskStatus } from '../types/task';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';
import { Calendar, Trash2, AlertTriangle, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

/**
 * TaskList component displays tasks in a Kanban board layout.
 * 
 * The Kanban board has four columns representing task statuses:
 * - TODO: Tasks waiting to be started
 * - IN_PROGRESS: Tasks currently being worked on
 * - REVIEW: Tasks waiting for review
 * - COMPLETED: Finished tasks
 * 
 * Drag-and-drop is powered by @hello-pangea/dnd library, which provides:
 * - DragDropContext: wraps the entire drag-and-drop area
 * - Droppable: defines areas where items can be dropped (columns)
 * - Draggable: defines items that can be dragged (task cards)
 * 
 * When a task is dragged from one column to another, the onDragEnd
 * callback updates the task's status via the API.
 */
interface TaskListProps {
    tasks: Task[];
    onStatusChange: (taskId: number, newStatus: string) => Promise<void>;
    onDeleteTask: (taskId: number) => Promise<void>;
}

// Column configuration - defines the Kanban board columns
const COLUMNS: { id: TaskStatus; title: string; color: string; bgColor: string }[] = [
    { id: 'TODO', title: 'To Do', color: 'text-gray-600', bgColor: 'bg-gray-100 dark:bg-gray-800' },
    { id: 'IN_PROGRESS', title: 'In Progress', color: 'text-blue-600', bgColor: 'bg-blue-50 dark:bg-blue-900/20' },
    { id: 'REVIEW', title: 'Review', color: 'text-yellow-600', bgColor: 'bg-yellow-50 dark:bg-yellow-900/20' },
    { id: 'COMPLETED', title: 'Completed', color: 'text-green-600', bgColor: 'bg-green-50 dark:bg-green-900/20' },
];

const TaskList: React.FC<TaskListProps> = ({ tasks, onStatusChange, onDeleteTask }) => {

    /**
     * Handle drag end event.
     * Called when a task card is dropped into a new column.
     * 
     * @param result - contains source (where it was) and destination (where it went)
     */
    const handleDragEnd = async (result: DropResult) => {
        const { destination, source, draggableId } = result;

        // If dropped outside a valid area, do nothing
        if (!destination) return;

        // If dropped in the same position, do nothing
        if (destination.droppableId === source.droppableId) return;

        const taskId = parseInt(draggableId);
        const newStatus = destination.droppableId;

        try {
            await onStatusChange(taskId, newStatus);
            toast.success(`Task moved to ${newStatus.replace('_', ' ')}`);
        } catch (error) {
            toast.error('Failed to update task status');
        }
    };

    /**
     * Get the priority badge CSS class based on priority level.
     */
    const getPriorityBadge = (priority: string): string => {
        switch (priority) {
            case 'LOW': return 'badge badge-low';
            case 'MEDIUM': return 'badge badge-medium';
            case 'HIGH': return 'badge badge-high';
            case 'URGENT': return 'badge badge-urgent';
            default: return 'badge badge-medium';
        }
    };

    /**
     * Check if a task is overdue (due date is before today).
     */
    const isOverdue = (dueDate: string | null): boolean => {
        if (!dueDate) return false;
        return new Date(dueDate) < new Date(new Date().toDateString());
    };

    /**
     * Format a date string for display.
     * Shows "Today", "Tomorrow", "Yesterday", or the formatted date.
     */
    const formatDate = (dateStr: string | null): string => {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const today = new Date(new Date().toDateString());
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        if (date.getTime() === today.getTime()) return 'Today';
        if (date.getTime() === tomorrow.getTime()) return 'Tomorrow';
        if (date.getTime() === yesterday.getTime()) return 'Yesterday';

        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    };

    return (
        <DragDropContext onDragEnd={handleDragEnd}>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {COLUMNS.map((column) => {
                    // Filter tasks for this column
                    const columnTasks = tasks.filter((task) => task.status === column.id);

                    return (
                        <div key={column.id} className={`rounded-xl p-3 ${column.bgColor} min-h-[400px]`}>
                            {/* Column Header */}
                            <div className="flex items-center justify-between mb-3 px-1">
                                <h3 className={`font-semibold text-sm ${column.color}`}>
                                    {column.title}
                                </h3>
                                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 bg-white dark:bg-gray-700 px-2 py-0.5 rounded-full">
                                    {columnTasks.length}
                                </span>
                            </div>

                            {/* Droppable Column */}
                            <Droppable droppableId={column.id}>
                                {(provided, snapshot) => (
                                    <div
                                        ref={provided.innerRef}
                                        {...provided.droppableProps}
                                        className={`space-y-2 min-h-[350px] rounded-lg p-1 transition-colors ${snapshot.isDraggingOver ? 'bg-primary-100/50 dark:bg-primary-900/20' : ''
                                            }`}
                                    >
                                        {columnTasks.map((task, index) => (
                                            <Draggable
                                                key={task.id}
                                                draggableId={String(task.id)}
                                                index={index}
                                            >
                                                {(provided, snapshot) => (
                                                    <div
                                                        ref={provided.innerRef}
                                                        {...provided.draggableProps}
                                                        {...provided.dragHandleProps}
                                                        className={`card p-3 cursor-grab active:cursor-grabbing ${snapshot.isDragging
                                                                ? 'shadow-lg ring-2 ring-primary-400 rotate-2'
                                                                : ''
                                                            }`}
                                                    >
                                                        {/* Task Title */}
                                                        <h4 className="font-medium text-sm text-gray-900 dark:text-white mb-1.5 line-clamp-2">
                                                            {task.title}
                                                        </h4>

                                                        {/* Task Description Preview */}
                                                        {task.description && (
                                                            <p className="text-xs text-gray-500 dark:text-gray-400 mb-2 line-clamp-2">
                                                                {task.description}
                                                            </p>
                                                        )}

                                                        {/* Priority Badge */}
                                                        <div className="flex items-center gap-2 mb-2">
                                                            <span className={getPriorityBadge(task.priority)}>
                                                                {task.priority}
                                                            </span>
                                                        </div>

                                                        {/* Footer: Due date + Delete button */}
                                                        <div className="flex items-center justify-between">
                                                            {task.dueDate && (
                                                                <div className={`flex items-center gap-1 text-xs ${isOverdue(task.dueDate) && task.status !== 'COMPLETED'
                                                                        ? 'text-red-500'
                                                                        : 'text-gray-500 dark:text-gray-400'
                                                                    }`}>
                                                                    {isOverdue(task.dueDate) && task.status !== 'COMPLETED' ? (
                                                                        <AlertTriangle size={12} />
                                                                    ) : (
                                                                        <Calendar size={12} />
                                                                    )}
                                                                    {formatDate(task.dueDate)}
                                                                </div>
                                                            )}
                                                            {!task.dueDate && <div />}

                                                            {/* Delete button */}
                                                            <button
                                                                onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    if (confirm('Delete this task?')) {
                                                                        onDeleteTask(task.id);
                                                                    }
                                                                }}
                                                                className="p-1 rounded text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors opacity-0 group-hover:opacity-100"
                                                                title="Delete task"
                                                            >
                                                                <Trash2 size={14} />
                                                            </button>
                                                        </div>

                                                        {/* Assignee */}
                                                        {task.assigneeName && (
                                                            <div className="flex items-center gap-1.5 mt-2 pt-2 border-t border-gray-100 dark:border-gray-700">
                                                                <div className="w-5 h-5 bg-primary-100 dark:bg-primary-900 rounded-full flex items-center justify-center">
                                                                    <span className="text-[10px] font-medium text-primary-600 dark:text-primary-400">
                                                                        {task.assigneeName.charAt(0).toUpperCase()}
                                                                    </span>
                                                                </div>
                                                                <span className="text-xs text-gray-500 dark:text-gray-400">
                                                                    {task.assigneeName}
                                                                </span>
                                                            </div>
                                                        )}
                                                    </div>
                                                )}
                                            </Draggable>
                                        ))}
                                        {provided.placeholder}

                                        {/* Empty column message */}
                                        {columnTasks.length === 0 && (
                                            <div className="flex flex-col items-center justify-center py-8 text-gray-400 dark:text-gray-500">
                                                <Clock size={24} className="mb-2 opacity-50" />
                                                <p className="text-xs">No tasks here</p>
                                                <p className="text-xs">Drag tasks to this column</p>
                                            </div>
                                        )}
                                    </div>
                                )}
                            </Droppable>
                        </div>
                    );
                })}
            </div>
        </DragDropContext>
    );
};

export default TaskList;
