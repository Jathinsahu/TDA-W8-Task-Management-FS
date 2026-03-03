package com.taskmanager.config;

import com.taskmanager.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig configures Spring Security for our application.
 * 
 * This is one of the most important configuration classes because it defines:
 * 1. Which endpoints require authentication and which are public
 * 2. How passwords are encoded (BCrypt hashing)
 * 3. Where our JWT filter sits in the filter chain
 * 4. Session management policy (stateless for JWT-based auth)
 * 
 * Spring Security uses a "filter chain" to process each request. Different
 * filters handle different security concerns (CORS, CSRF, authentication, etc.).
 * We insert our JwtAuthenticationFilter into this chain to handle JWT-based auth.
 * 
 * Key decisions explained:
 * - CSRF disabled: We use JWT tokens instead of session cookies, so CSRF
 *   protection isn't needed (CSRF exploits cookie-based sessions).
 * - Stateless sessions: The server doesn't store session state. Each request
 *   is independently authenticated via its JWT token.
 * - H2 console access: We allow frame options for the H2 database console
 *   during development (it uses iframes internally).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Defines the security filter chain - the heart of security configuration.
     * 
     * This method configures:
     * - CSRF protection (disabled for REST APIs)
     * - CORS (handled by CorsConfig)
     * - URL-based authorization rules
     * - Session management (stateless)
     * - JWT filter placement
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF - not needed for stateless JWT authentication
            .csrf(AbstractHttpConfigurer::disable)

            // Allow H2 console to work (it uses frames internally)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )

            // Configure URL-based authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/ws/**").permitAll()

                // Allow preflight OPTIONS requests (needed for CORS)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Use stateless session management (no server-side sessions)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Set our custom authentication provider
            .authenticationProvider(authenticationProvider())

            // Add our JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder bean.
     * 
     * BCrypt is a one-way hashing algorithm specifically designed for passwords.
     * It includes a built-in salt (random value added before hashing) which means
     * even if two users have the same password, their hashes will be different.
     * 
     * The default strength is 10, which means 2^10 = 1024 rounds of hashing.
     * This makes brute-force attacks very slow.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider bean that uses our UserDetailsService and PasswordEncoder.
     * 
     * DaoAuthenticationProvider is the standard implementation that:
     * 1. Uses UserDetailsService to load user from database
     * 2. Uses PasswordEncoder to compare submitted password with stored hash
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager bean - needed by our AuthService to authenticate users.
     * We get the default manager from Spring's AuthenticationConfiguration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
