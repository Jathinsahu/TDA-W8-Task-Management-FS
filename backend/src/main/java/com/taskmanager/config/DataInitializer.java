package com.taskmanager.config;

import com.taskmanager.model.entity.Task;
import com.taskmanager.model.entity.User;
import com.taskmanager.model.enums.Role;
import com.taskmanager.model.enums.TaskPriority;
import com.taskmanager.model.enums.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * DataInitializer seeds the database with sample data on application startup.
 * 
 * This is extremely useful during development because:
 * 1. We use H2 in-memory database which resets every time the app restarts
 * 2. It creates demo accounts so we can log in immediately
 * 3. It creates sample tasks so the Kanban board isn't empty
 * 
 * CommandLineRunner is a Spring Boot interface that runs the run() method
 * after the application starts. This is the perfect place to seed data.
 * 
 * Demo Accounts:
 * - Admin: admin@taskmanager.com / admin123
 * - User:  user@taskmanager.com  / user123
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only seed data if the database is empty
        if (userRepository.count() > 0) {
            logger.info("Database already has data, skipping initialization");
            return;
        }

        logger.info("Seeding database with sample data...");

        // Create demo admin user
        User admin = User.builder()
                .name("Admin User")
                .email("admin@taskmanager.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        admin = userRepository.save(admin);

        // Create demo regular user
        User user = User.builder()
                .name("John Doe")
                .email("user@taskmanager.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);

        // Create sample tasks across different statuses
        taskRepository.save(Task.builder()
                .title("Set up project structure")
                .description("Initialize the React frontend and Spring Boot backend with all necessary dependencies and configurations.")
                .status(TaskStatus.COMPLETED)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(5))
                .assignee(admin)
                .createdBy(admin)
                .build());

        taskRepository.save(Task.builder()
                .title("Implement user authentication")
                .description("Create login and registration forms with JWT token handling. Include token refresh mechanism and secure storage.")
                .status(TaskStatus.COMPLETED)
                .priority(TaskPriority.URGENT)
                .dueDate(LocalDate.now().minusDays(3))
                .assignee(user)
                .createdBy(admin)
                .build());

        taskRepository.save(Task.builder()
                .title("Design Kanban board UI")
                .description("Create the drag-and-drop Kanban board with columns for TODO, IN_PROGRESS, REVIEW, and COMPLETED statuses.")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(2))
                .assignee(user)
                .createdBy(admin)
                .build());

        taskRepository.save(Task.builder()
                .title("Add real-time notifications")
                .description("Set up WebSocket connections for real-time task updates. When a task is modified, all connected users should see the change.")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(5))
                .assignee(admin)
                .createdBy(user)
                .build());

        taskRepository.save(Task.builder()
                .title("Write API documentation")
                .description("Document all REST endpoints using Swagger annotations. Include request/response examples for each endpoint.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .assignee(user)
                .createdBy(admin)
                .build());

        taskRepository.save(Task.builder()
                .title("Add dark mode support")
                .description("Implement a dark/light theme toggle that persists across sessions using localStorage.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now().plusDays(10))
                .assignee(user)
                .createdBy(user)
                .build());

        taskRepository.save(Task.builder()
                .title("Set up Docker containers")
                .description("Create Dockerfiles for frontend and backend, and a docker-compose.yml for running the entire stack.")
                .status(TaskStatus.REVIEW)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(1))
                .assignee(admin)
                .createdBy(admin)
                .build());

        taskRepository.save(Task.builder()
                .title("Implement task search")
                .description("Add search functionality that filters tasks by title and description with debounced input.")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(8))
                .assignee(admin)
                .createdBy(user)
                .build());

        logger.info("Database seeded with {} users and {} tasks",
                userRepository.count(), taskRepository.count());
    }
}
