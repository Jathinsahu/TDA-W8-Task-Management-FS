package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Task Manager Backend Application.
 * 
 * The @SpringBootApplication annotation is a convenience annotation that combines:
 * - @Configuration: Marks this class as a source of bean definitions
 * - @EnableAutoConfiguration: Tells Spring Boot to auto-configure beans based on dependencies
 * - @ComponentScan: Scans for Spring components in this package and sub-packages
 * 
 * When we run this main method, Spring Boot:
 * 1. Starts the embedded Tomcat server
 * 2. Sets up the database connection (H2 or PostgreSQL)
 * 3. Creates all the REST endpoints we defined in our controllers
 * 4. Configures security with our JWT authentication
 * 5. Sets up WebSocket for real-time communication
 */
@SpringBootApplication
public class TaskManagerApplication {

    public static void main(String[] args) {
        // SpringApplication.run() bootstraps the entire application
        // It creates the ApplicationContext and starts the embedded web server
        SpringApplication.run(TaskManagerApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("  Task Manager Backend is running!");
        System.out.println("  API: http://localhost:8080/api");
        System.out.println("  Swagger: http://localhost:8080/swagger-ui.html");
        System.out.println("  H2 Console: http://localhost:8080/h2-console");
        System.out.println("===========================================\n");
    }
}
