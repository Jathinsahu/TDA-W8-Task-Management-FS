package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.model.dto.LoginRequest;
import com.taskmanager.model.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the AuthController endpoints.
 * 
 * These tests use MockMvc to simulate HTTP requests to our REST API
 * without actually starting a web server. This makes tests faster
 * and more reliable.
 * 
 * @SpringBootTest loads the full application context
 * @AutoConfigureMockMvc provides the MockMvc instance
 * @TestMethodOrder ensures tests run in a specific order
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test successful user registration.
     * Expects a 201 CREATED response with JWT tokens.
     */
    @Test
    @Order(1)
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "test@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * Test duplicate email registration.
     * Expects a 400 BAD REQUEST because the email is already registered.
     */
    @Test
    @Order(2)
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Another User",
                "test@example.com",
                "password456"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test successful login with seeded demo account.
     * The DataInitializer creates admin@taskmanager.com / admin123.
     */
    @Test
    @Order(3)
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest(
                "admin@taskmanager.com",
                "admin123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@taskmanager.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * Test login with wrong password.
     * Expects a 401 UNAUTHORIZED response.
     */
    @Test
    @Order(4)
    void testLoginWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest(
                "admin@taskmanager.com",
                "wrongpassword"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test registration with invalid data (missing required fields).
     * Expects a 400 BAD REQUEST with validation error details.
     */
    @Test
    @Order(5)
    void testRegisterValidation() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "",     // Empty name - should fail validation
                "invalid-email",  // Invalid email format
                "12"    // Too short password
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
