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
}
