import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WebSocketMessage } from '../types/task';

/**
 * WebSocketService manages the real-time connection to the Spring Boot backend.
 * 
 * We use STOMP (Simple Text Oriented Messaging Protocol) over SockJS for
 * real-time communication. This allows the server to push task updates
 * to all connected clients instantly.
 * 
 * How it works:
 * 1. Client connects to ws://localhost:8080/ws via SockJS
 * 2. STOMP protocol is used on top of the WebSocket connection
 * 3. Client subscribes to /topic/tasks to receive task updates
 * 4. When any user creates/updates/deletes a task, the server
 *    broadcasts the change to all subscribers
 * 5. Our React components update their state based on these messages
 * 
 * SockJS provides fallback transports (long-polling, etc.) for
 * browsers that don't support WebSocket natively.
 */
class WebSocketService {
    private client: Client | null = null;
    private messageHandlers: ((message: WebSocketMessage) => void)[] = [];

    /**
     * Connect to the WebSocket server.
     * Called after the user logs in successfully.
     */
    connect(): void {
        const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

        this.client = new Client({
            // Use SockJS as the WebSocket factory for better browser support
            webSocketFactory: () => new SockJS(wsUrl) as WebSocket,

            // Reconnect settings - try to reconnect every 5 seconds
            reconnectDelay: 5000,

            // Heartbeat to keep the connection alive
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,

            // Called when the connection is established
            onConnect: () => {
                console.log('WebSocket connected');

                // Subscribe to task updates
                this.client?.subscribe('/topic/tasks', (message) => {
                    try {
                        const parsedMessage: WebSocketMessage = JSON.parse(message.body);
                        // Notify all registered handlers about the new message
                        this.messageHandlers.forEach((handler) => handler(parsedMessage));
                    } catch (error) {
                        console.error('Error parsing WebSocket message:', error);
                    }
                });
            },

            // Called when the connection is lost
            onDisconnect: () => {
                console.log('WebSocket disconnected');
            },

            // Called on connection errors
            onStompError: (frame) => {
                console.error('WebSocket STOMP error:', frame.headers['message']);
            },
        });

        // Start the connection
        this.client.activate();
    }

    /**
     * Disconnect from the WebSocket server.
     * Called when the user logs out.
     */
    disconnect(): void {
        if (this.client) {
            this.client.deactivate();
            this.client = null;
        }
        this.messageHandlers = [];
    }

    /**
     * Register a handler function to be called when a message is received.
     * Returns an unsubscribe function to remove the handler.
     * 
     * Usage in a React component:
     * useEffect(() => {
     *   const unsubscribe = webSocketService.onMessage((msg) => { ... });
     *   return unsubscribe; // Cleanup on unmount
     * }, []);
     */
    onMessage(handler: (message: WebSocketMessage) => void): () => void {
        this.messageHandlers.push(handler);

        // Return cleanup function
        return () => {
            this.messageHandlers = this.messageHandlers.filter((h) => h !== handler);
        };
    }
}

// Export a single instance
export const webSocketService = new WebSocketService();
