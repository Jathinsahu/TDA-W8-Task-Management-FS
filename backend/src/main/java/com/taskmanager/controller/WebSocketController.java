package com.taskmanager.controller;

import com.taskmanager.model.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocketController handles STOMP messages from connected clients.
 * 
 * While most of our real-time communication is server-to-client (pushing
 * task updates), this controller handles the client-to-server direction.
 * 
 * Clients can send messages to /app/tasks.update, and the response
 * is broadcast to all subscribers of /topic/tasks.
 * 
 * In our architecture:
 * - The TaskService sends most WebSocket notifications (via WebSocketService)
 * - This controller handles any direct client-to-server WebSocket messages
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    /**
     * Handle task update messages from clients.
     * 
     * @MessageMapping maps STOMP messages to this method.
     * When a client sends to /app/tasks.update, this method processes it
     * and the result is sent to /topic/tasks (via @SendTo).
     */
    @MessageMapping("/tasks.update")
    @SendTo("/topic/tasks")
    public TaskDto handleTaskUpdate(TaskDto taskDto) {
        return taskDto;
    }
}
