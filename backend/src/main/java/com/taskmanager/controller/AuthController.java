package com.taskmanager.controller;

import com.taskmanager.model.dto.AuthResponse;
import com.taskmanager.model.dto.LoginRequest;
import com.taskmanager.model.dto.RegisterRequest;
import com.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController handles HTTP requests for authentication operations.
 * 
 * This controller exposes REST endpoints for:
 * - POST /api/auth/register - Create a new user account
 * - POST /api/auth/login    - Authenticate and get JWT tokens
 * - POST /api/auth/refresh  - Refresh an expired access token
 * - POST /api/auth/logout   - Invalidate the refresh token
 * 
 * All these endpoints are public (no authentication required),
 * as configured in SecurityConfig. This makes sense because you
 * need to be able to log in before you can use a JWT token.
 * 
 * The controller follows the REST convention:
 * - POST for creating resources or performing actions
 * - ResponseEntity for explicit HTTP status codes
 * - @Valid for request body validation
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Register a new user account.
     * 
     * Request body: { "name": "John", "email": "john@example.com", "password": "pass123" }
     * Response: { "accessToken": "...", "refreshToken": "...", "userId": 1, ... }
     * 
     * Returns 201 CREATED on success because we're creating a new resource (user).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate a user and return JWT tokens.
     * 
     * Request body: { "email": "john@example.com", "password": "pass123" }
     * Response: { "accessToken": "...", "refreshToken": "...", "userId": 1, ... }
     * 
     * If credentials are wrong, Spring Security throws AuthenticationException
     * which is handled by our GlobalExceptionHandler.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh an expired access token.
     * 
     * Request body: { "refreshToken": "uuid-string-here" }
     * Response: { "accessToken": "new-token", "refreshToken": "new-refresh-token", ... }
     * 
     * The frontend calls this automatically when it gets a 401 response.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Log out a user by invalidating their refresh token.
     * 
     * Request body: { "refreshToken": "uuid-string-here" }
     * 
     * Returns 204 NO CONTENT because we're not returning any data.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }
}
