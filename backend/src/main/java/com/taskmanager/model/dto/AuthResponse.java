package com.taskmanager.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse is returned after a successful login or token refresh.
 * 
 * It contains two JWT tokens:
 * - accessToken: Short-lived token (24 hours) used for API authentication.
 *   The frontend includes this in the Authorization header of every request.
 * - refreshToken: Long-lived token (7 days) used to get a new access token
 *   when the current one expires, without requiring the user to log in again.
 * 
 * It also includes basic user information so the frontend can display
 * the user's name and role without making an additional API call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String name;
    private String email;
    private String role;
}
