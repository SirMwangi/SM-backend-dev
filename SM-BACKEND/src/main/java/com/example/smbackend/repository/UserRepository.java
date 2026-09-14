package com.example.smbackend.repository;

import com.example.smbackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for managing User entity persistence.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a User entity by its unique email address.
     *
     * @param email The email address to look up
     * @return An Optional containing the User if found, or empty Optional if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a User entity by its phone number.
     *
     * @param phoneNumber The phone number to look up
     * @return An Optional containing the User if found, or empty Optional if not found
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Checks if a user exists with the given phone number.
     *
     * @param phoneNumber The phone number to check
     * @return true if a user exists with the given phone number, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);
}
