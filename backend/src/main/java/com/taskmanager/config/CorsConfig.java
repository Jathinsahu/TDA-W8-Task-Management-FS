package com.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CorsConfig configures Cross-Origin Resource Sharing (CORS) for our backend.
 * 
 * CORS is a browser security feature that blocks web pages from making
 * requests to a different domain than the one they were loaded from.
 * 
 * For example, our React frontend runs on http://localhost:3000, but our
 * Spring Boot API runs on http://localhost:8080. Without CORS configuration,
 * the browser would block the frontend from calling our API.
 * 
 * This configuration tells the browser: "It's okay for these specific
 * origins to make requests to our server."
 * 
 * We allow:
 * - Origins: localhost:3000 (React dev server) and localhost:5173 (Vite)
 * - Methods: GET, POST, PUT, DELETE, OPTIONS
 * - Headers: Authorization (for JWT), Content-Type, etc.
 * - Credentials: true (allows cookies and auth headers)
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configure CORS mappings for all endpoints.
     * This registers CORS rules with Spring MVC.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                      // Apply to all endpoints
                .allowedOrigins(
                        "http://localhost:3000",         // React default port
                        "http://localhost:5173",         // Vite default port
                        "http://localhost:4173"          // Vite preview port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")                    // Allow all headers
                .allowCredentials(true)                 // Allow auth credentials
                .maxAge(3600);                          // Cache preflight for 1 hour
    }

    /**
     * CORS configuration source bean for Spring Security.
     * Spring Security has its own CORS processing that runs before
     * Spring MVC, so we need to configure both.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:4173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
