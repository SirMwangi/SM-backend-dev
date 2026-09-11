package com.example.smbackend.service;

import com.example.smbackend.domain.User;
import com.example.smbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Isolated unit test for UserService executing purely with Mockito.
 * No Spring context or database connection is initialized.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWithHashedPassword() {
        // Arrange
        String name = "Jane Doe";
        String email = "jane@example.com";
        String rawPassword = "rawPassword123";
        String hashedPassword = "hashed_secret_123";

        User savedUser = new User(name, email, hashedPassword);

        when(passwordEncoder.encode("rawPassword123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User actualUser = userService.createUser("Jane Doe", "jane@example.com", "rawPassword123");

        // Assert & Verify
        assertNotNull(actualUser, "The returned user must not be null");
        assertEquals("jane@example.com", actualUser.getEmail(), "The user email must match jane@example.com");

        verify(passwordEncoder, times(1)).encode("rawPassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
