package com.taskmanager.service;

import com.taskmanager.model.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocketService handles sending real-time notifications to connected clients.
 * 
 * When a task is created, updated, or deleted, this service broadcasts
 * the change to all connected clients via WebSocket. This way, every user
 * viewing the Kanban board sees the change immediately without refreshing.
 * 
 * SimpMessagingTemplate is provided by Spring's WebSocket STOMP support.
 * It allows us to send messages to specific STOMP destinations.
 * Clients subscribe to these destinations to receive updates.
 * 
 * Message format:
 * {
 *   "type": "TASK_CREATED" | "TASK_UPDATED" | "TASK_DELETED",
 *   "payload": { ... task data or task id ... }
 * }
 */
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    // Spring's messaging template for sending STOMP messages
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notify all clients that a new task was created.
     * Clients use this to add the new task card to their Kanban board.
     */
    public void notifyTaskCreated(TaskDto task) {
        logger.debug("Broadcasting task created: {}", task.getId());
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TASK_CREATED");
        message.put("payload", task);

        // Send to /topic/tasks - all subscribers will receive this
        messagingTemplate.convertAndSend("/topic/tasks", message);
    }

    /**
     * Notify all clients that a task was updated.
     * Clients use this to update the task card on their Kanban board.
     */
    public void notifyTaskUpdated(TaskDto task) {
        logger.debug("Broadcasting task updated: {}", task.getId());
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TASK_UPDATED");
        message.put("payload", task);

        messagingTemplate.convertAndSend("/topic/tasks", message);
    }

    /**
     * Notify all clients that a task was deleted.
     * Clients use this to remove the task card from their Kanban board.
     */
    public void notifyTaskDeleted(Long taskId) {
        logger.debug("Broadcasting task deleted: {}", taskId);
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TASK_DELETED");
        message.put("payload", taskId);

        messagingTemplate.convertAndSend("/topic/tasks", message);
    }
}
