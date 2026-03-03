package com.taskmanager.service;

import com.taskmanager.model.dto.AuthResponse;
import com.taskmanager.model.dto.LoginRequest;
import com.taskmanager.model.dto.RegisterRequest;
import com.taskmanager.model.entity.User;
import com.taskmanager.model.enums.Role;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService handles all authentication-related business logic.
 * 
 * This service is responsible for:
 * 1. User registration - creating new accounts with hashed passwords
 * 2. User login - validating credentials and issuing JWT tokens
 * 3. Token refresh - issuing new access tokens using refresh tokens
 * 4. Logout - invalidating refresh tokens
 * 
 * It also implements UserDetailsService, which Spring Security uses
 * to load user information during authentication. This is the bridge
 * between our custom User entity and Spring Security's authentication.
 * 
 * The @Transactional annotation ensures database operations are atomic.
 * If any operation fails, all changes within the method are rolled back.
 */
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Load user by username (email) - required by UserDetailsService.
     * 
     * Spring Security calls this method during authentication to get
     * the user's details (password hash, roles, etc.) from the database.
     * 
     * @param email the user's email address
     * @return UserDetails object (our User entity implements UserDetails)
     * @throws UsernameNotFoundException if no user with that email exists
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));
    }

    /**
     * Register a new user account.
     * 
     * Steps:
     * 1. Check if email is already taken
     * 2. Hash the password using BCrypt
     * 3. Create and save the new user
     * 4. Log them in automatically and return tokens
     * 
     * @param request the registration details (name, email, password)
     * @return AuthResponse with JWT tokens and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user with email: {}", request.getEmail());

        // Check if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered: " + request.getEmail());
        }

        // Create a new user with hashed password
        // NEVER store plain text passwords!
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        // Save to database
        user = userRepository.save(user);
        logger.info("User registered successfully: {}", user.getEmail());

        // Auto-login: authenticate the user and generate tokens
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        return generateAuthResponse(authentication, user);
    }

    /**
     * Authenticate a user and issue JWT tokens.
     * 
     * Steps:
     * 1. Validate credentials using AuthenticationManager
     * 2. Generate access and refresh tokens
     * 3. Store refresh token in the database
     * 4. Return tokens and user info
     * 
     * @param request the login credentials (email, password)
     * @return AuthResponse with JWT tokens and user info
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        // AuthenticationManager validates the credentials
        // If invalid, it throws an AuthenticationException
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get the authenticated user
        User user = (User) authentication.getPrincipal();
        logger.info("User logged in successfully: {}", user.getEmail());

        return generateAuthResponse(authentication, user);
    }

    /**
     * Refresh an expired access token using a valid refresh token.
     * 
     * This allows the frontend to get a new access token without asking
     * the user to log in again. The flow is:
     * 1. Access token expires
     * 2. Frontend sends refresh token to this endpoint
     * 3. We verify the refresh token and issue new tokens
     * 
     * @param refreshToken the refresh token to validate
     * @return AuthResponse with new JWT tokens
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Token refresh requested");

        // Find the user associated with this refresh token
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUsername(user.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();

        // Update the stored refresh token
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        logger.info("Token refreshed successfully for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Log out a user by invalidating their refresh token.
     * 
     * After logout, the refresh token can no longer be used to get
     * new access tokens. The access token will naturally expire
     * after its TTL (24 hours).
     * 
     * @param refreshToken the refresh token to invalidate
     */
    @Transactional
    public void logout(String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    user.setRefreshToken(null);
                    userRepository.save(user);
                    logger.info("User logged out: {}", user.getEmail());
                });
    }

    /**
     * Helper method to generate an AuthResponse with tokens.
     * Used by both login and register methods.
     */
    private AuthResponse generateAuthResponse(Authentication authentication, User user) {
        // Generate new JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // Store the refresh token in the database
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // Build and return the response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
