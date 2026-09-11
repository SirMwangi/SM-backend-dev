package com.example.smbackend.service;

import com.example.smbackend.domain.User;
import com.example.smbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class orchestrating User lifecycle and business logic.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Dependency Injection via constructor:
     * Injects UserRepository for database operations and Spring Security's PasswordEncoder
     * for secure one-way credential hashing.
     *
     * @param userRepository  Repository handling User database operations
     * @param passwordEncoder Cryptographic encoder used to hash plaintext passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Hashes the raw plaintext password and saves the new User entity to the database.
     *
     * @param name        The user's display name
     * @param email       The user's unique email address
     * @param rawPassword The plaintext password to be encoded before persistence
     * @return The persisted User entity returned by the repository
     */
    public User createUser(String name, String email, String rawPassword) {
        // Step 1: Securely encode the plaintext password using the injected PasswordEncoder
        String passwordHash = passwordEncoder.encode(rawPassword);

        // Step 2: Create a new User entity and assign the generated password hash
        User user = new User(name, email, passwordHash);

        // Step 3: Persist and return the User entity via UserRepository
        return userRepository.save(user);
    }
}
