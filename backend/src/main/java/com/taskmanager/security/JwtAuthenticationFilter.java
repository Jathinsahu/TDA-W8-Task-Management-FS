package com.taskmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter intercepts every HTTP request to check for JWT tokens.
 * 
 * This is a crucial part of our security setup. Here's how it works:
 * 
 * 1. When a request comes in, this filter runs BEFORE the request reaches
 *    our controller methods.
 * 
 * 2. It checks the "Authorization" header for a JWT token.
 *    The token should be in the format: "Bearer <token>"
 * 
 * 3. If a valid token is found:
 *    - Extract the username (email) from the token
 *    - Load the user details from the database
 *    - Create an Authentication object and put it in the SecurityContext
 *    - Now Spring Security knows who the user is for this request
 * 
 * 4. If no token is found or the token is invalid:
 *    - The SecurityContext remains empty
 *    - The request continues, but secured endpoints will return 401 Unauthorized
 * 
 * We extend OncePerRequestFilter to guarantee this filter runs exactly once
 * per request, even if the request is forwarded internally.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Injected via constructor (RequiredArgsConstructor + final)
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Main filter method - called for every HTTP request.
     * 
     * The FilterChain represents the remaining filters and the actual
     * endpoint handler. We MUST call filterChain.doFilter() to let the
     * request continue to the next filter/controller. If we don't call it,
     * the request will be blocked.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Step 1: Extract JWT token from the Authorization header
            String jwt = getJwtFromRequest(request);

            // Step 2: If we have a token and it's valid, authenticate the user
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // Step 3: Get the username from the token
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // Step 4: Load full user details from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5: Create an authentication token
                // This tells Spring Security "this user is authenticated"
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                    // The principal (our User object)
                                null,                           // Credentials (not needed after auth)
                                userDetails.getAuthorities()    // User's roles/permissions
                        );

                // Step 6: Add request details (IP address, session ID, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 7: Store the authentication in the SecurityContext
                // Now any code that calls SecurityContextHolder.getContext().getAuthentication()
                // will get this authentication object with the user's details
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // If anything goes wrong, log it but don't stop the request.
            // The request will continue without authentication, and secured
            // endpoints will return 401 naturally.
            logger.error("Could not set user authentication in security context", ex);
        }

        // IMPORTANT: Always call this to continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract the JWT token from the Authorization header.
     * 
     * The standard format is: Authorization: Bearer <token>
     * We remove the "Bearer " prefix to get just the token string.
     * 
     * @param request the incoming HTTP request
     * @return the JWT token string, or null if not present
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
}
