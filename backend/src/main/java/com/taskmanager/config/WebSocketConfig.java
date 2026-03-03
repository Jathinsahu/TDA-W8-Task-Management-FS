package com.taskmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig configures STOMP-based WebSocket messaging.
 * 
 * WebSocket provides full-duplex (two-way) communication between the
 * client and server over a single TCP connection. Unlike HTTP (where
 * the client always initiates), WebSocket allows the server to push
 * updates to connected clients in real-time.
 * 
 * We use STOMP (Simple Text Oriented Messaging Protocol) on top of
 * WebSocket as the messaging protocol. STOMP provides:
 * - Topic-based pub/sub messaging (subscribe to "/topic/tasks")
 * - Message routing (send to specific destinations)
 * - Header support for metadata
 * 
 * SockJS fallback is enabled so older browsers that don't support
 * WebSocket can still receive real-time updates using alternative
 * transport mechanisms (long-polling, iframe, etc.).
 * 
 * In our application, WebSocket is used to:
 * - Notify all connected clients when a task is created, updated, or deleted
 * - Show real-time status changes on the Kanban board
 * - Display notifications for task assignments
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure the message broker.
     * 
     * The message broker handles routing messages to the right subscribers.
     * - /topic: prefix for broadcast messages (sent to all subscribers)
     * - /app: prefix for messages sent from the client to the server
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory broker for /topic destinations
        // Clients can subscribe to /topic/tasks to get task updates
        config.enableSimpleBroker("/topic");

        // Messages from clients with this prefix are routed to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Register STOMP endpoints that clients can connect to.
     * 
     * The "/ws" endpoint is where clients establish their WebSocket connection.
     * We enable SockJS fallback for browsers that don't support WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:4173"
                )
                .withSockJS();  // Enable SockJS fallback
    }
}
