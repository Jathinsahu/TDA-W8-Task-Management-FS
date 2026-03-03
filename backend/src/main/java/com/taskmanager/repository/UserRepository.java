package com.taskmanager.repository;

import com.taskmanager.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository provides database operations for the User entity.
 * 
 * By extending JpaRepository, we get all basic CRUD operations for free:
 * - save(), findById(), findAll(), deleteById(), count(), etc.
 * 
 * We only need to declare custom query methods here. Spring Data JPA
 * automatically implements these methods based on their names!
 * For example, "findByEmail" tells Spring to generate a query like:
 * SELECT * FROM users WHERE email = ?
 * 
 * This is called "query derivation" - one of the most powerful features
 * of Spring Data JPA. No need to write SQL or JPQL manually.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Returns Optional because the user might not exist.
     * Used during authentication to look up the user trying to log in.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     * Used during registration to prevent duplicate accounts.
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by their refresh token.
     * Used during token refresh to identify which user the token belongs to.
     */
    Optional<User> findByRefreshToken(String refreshToken);
}
