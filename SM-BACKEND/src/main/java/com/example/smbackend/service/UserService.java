package com.example.smbackend.service;

import com.example.smbackend.domain.User;
import com.example.smbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

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
     * Hashes the raw plaintext password and saves the new User entity to the database without a phone number.
     *
     * @param name        The user's display name
     * @param email       The user's unique email address
     * @param rawPassword The plaintext password to be encoded before persistence
     * @return The persisted User entity returned by the repository
     */
    public User createUser(String name, String email, String rawPassword) {
        return createUser(name, email, null, rawPassword);
    }

    /**
     * Hashes the raw plaintext password and saves the new User entity with a phone number to the database.
     *
     * @param name        The user's display name
     * @param email       The user's unique email address
     * @param phoneNumber The user's contact phone number (optional, 7-20 characters)
     * @param rawPassword The plaintext password to be encoded before persistence
     * @return The persisted User entity returned by the repository
     */
    public User createUser(String name, String email, String phoneNumber, String rawPassword) {
        String validatedPhoneNumber = normalizeAndValidatePhoneNumber(phoneNumber);

        // Step 1: Securely encode the plaintext password using the injected PasswordEncoder
        String passwordHash = passwordEncoder.encode(rawPassword);

        // Step 2: Create a new User entity and assign the generated password hash and phone number
        User user = new User(name, email, validatedPhoneNumber, passwordHash);

        // Step 3: Persist and return the User entity via UserRepository
        return userRepository.save(user);
    }

    /**
     * Updates an existing user's phone number.
     *
     * @param userId         The unique identifier of the user
     * @param newPhoneNumber The new phone number to set (optional, 7-20 characters, or null to clear)
     * @return The updated and persisted User entity
     * @throws IllegalArgumentException If no user is found with the provided ID or phone number is invalid
     */
    public User updatePhoneNumber(UUID userId, String newPhoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        String validatedPhoneNumber = normalizeAndValidatePhoneNumber(newPhoneNumber);
        user.setPhoneNumber(validatedPhoneNumber);

        return userRepository.save(user);
    }

    /**
     * Removes the phone number from an existing user.
     *
     * @param userId The unique identifier of the user
     * @return The updated and persisted User entity
     * @throws IllegalArgumentException If no user is found with the provided ID
     */
    public User removePhoneNumber(UUID userId) {
        return updatePhoneNumber(userId, null);
    }

    /**
     * Retrieves a user by their phone number.
     *
     * @param phoneNumber The phone number to look up
     * @return An Optional containing the matching User if found, or empty Optional
     */
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByPhoneNumber(phoneNumber.trim());
    }

    /**
     * Checks if a user already exists with the given phone number.
     *
     * @param phoneNumber The phone number to check
     * @return true if a user exists with the given phone number, false otherwise
     */
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByPhoneNumber(phoneNumber.trim());
    }

    /**
     * Finds a user by their unique ID.
     *
     * @param userId The unique identifier of the user
     * @return An Optional containing the User if found, or empty Optional
     */
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Normalizes and validates the phone number format according to the schema constraints.
     * A valid phone number must have length between 7 and 20 characters when present.
     *
     * @param phoneNumber The raw phone number string
     * @return Trimmed phone number, or null if null or empty
     * @throws IllegalArgumentException If phone number is non-blank but outside 7 to 20 characters
     */
    private String normalizeAndValidatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String trimmed = phoneNumber.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() < 7 || trimmed.length() > 20) {
            throw new IllegalArgumentException("Phone number must be between 7 and 20 characters");
        }
        return trimmed;
    }
}
