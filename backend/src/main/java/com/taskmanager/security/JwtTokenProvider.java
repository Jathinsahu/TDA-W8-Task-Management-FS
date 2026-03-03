package com.taskmanager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JwtTokenProvider handles all JWT token operations in our application.
 * 
 * JWT (JSON Web Token) is a standard for securely transmitting information
 * between the client and server. Instead of using sessions (which require
 * server-side storage), JWTs are self-contained tokens that include all
 * the information needed to identify a user.
 * 
 * A JWT has three parts, separated by dots:
 * 1. Header - specifies the algorithm used (HS256) and token type (JWT)
 * 2. Payload - contains claims (user data like email, expiration time)
 * 3. Signature - ensures the token hasn't been tampered with
 * 
 * Our application uses two types of tokens:
 * - Access Token: Short-lived (24 hours), sent with every API request.
 *   If someone steals this token, they only have access for a limited time.
 * - Refresh Token: Long-lived (7 days), used to get new access tokens.
 *   Stored securely and only sent when the access token expires.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // These values are injected from application.yml
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Creates a cryptographic signing key from our secret string.
     * The key is used to sign tokens (proving we created them) and
     * verify tokens (confirming they haven't been tampered with).
     * 
     * HMAC-SHA is a symmetric algorithm, meaning the same key is used
     * for both signing and verification. This works well for our use case
     * because only our server needs to create and verify tokens.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate an access token for an authenticated user.
     * 
     * The token includes:
     * - Subject: the user's email (unique identifier)
     * - IssuedAt: when the token was created
     * - Expiration: when the token expires (24 hours from now)
     * - Signature: proves the token was created by our server
     * 
     * @param authentication the authenticated user from Spring Security
     * @return the JWT access token as a string
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())       // Set the subject (email)
                .issuedAt(now)                            // Set issue time
                .expiration(expiryDate)                   // Set expiry time
                .signWith(getSigningKey())                // Sign with our secret key
                .compact();                               // Build the token string
    }

    /**
     * Generate an access token directly from a username/email.
     * Used during token refresh when we don't have an Authentication object.
     */
    public String generateAccessTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate a refresh token.
     * This is a random UUID string - it doesn't need to contain user information
     * because we store the mapping in the database (User.refreshToken field).
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Extract the username (email) from a JWT token.
     * We parse the token, verify the signature, and read the subject claim.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())      // Verify signature with our key
                .build()
                .parseSignedClaims(token)         // Parse the token
                .getPayload();                    // Get the claims (payload)

        return claims.getSubject();               // Return the subject (email)
    }

    /**
     * Validate a JWT token.
     * 
     * This method checks:
     * 1. The signature is valid (token wasn't tampered with)
     * 2. The token hasn't expired
     * 3. The token format is correct
     * 
     * If any check fails, it logs the error and returns false.
     * 
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}
