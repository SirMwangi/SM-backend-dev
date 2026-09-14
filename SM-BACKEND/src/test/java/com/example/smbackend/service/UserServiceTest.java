package com.example.smbackend.service;

import com.example.smbackend.domain.User;
import com.example.smbackend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    @DisplayName("Should create user with hashed password without phone number")
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
        assertNull(actualUser.getPhoneNumber(), "The phone number should be null");

        verify(passwordEncoder, times(1)).encode("rawPassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should create user with hashed password and phone number")
    void shouldCreateUserWithHashedPasswordAndPhoneNumber() {
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        String phoneNumber = "+254712345678";
        String rawPassword = "secretPassword!";
        String hashedPassword = "hashed_secret_xyz";

        User savedUser = new User(name, email, phoneNumber, hashedPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User actualUser = userService.createUser(name, email, phoneNumber, rawPassword);

        // Assert & Verify
        assertNotNull(actualUser);
        assertEquals("john@example.com", actualUser.getEmail());
        assertEquals("+254712345678", actualUser.getPhoneNumber());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("+254712345678", userCaptor.getValue().getPhoneNumber());
        assertEquals(hashedPassword, userCaptor.getValue().getPasswordHash());
    }

    @Test
    @DisplayName("Should reject user creation when phone number is too short")
    void shouldRejectCreationWhenPhoneNumberTooShort() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("John", "john@example.com", "12345", "password")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject user creation when phone number is too long")
    void shouldRejectCreationWhenPhoneNumberTooLong() {
        String overlyLongPhone = "123456789012345678901"; // 21 chars
        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("John", "john@example.com", overlyLongPhone, "password")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user's phone number successfully")
    void shouldUpdatePhoneNumberSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = new User(userId, "Jane Doe", "jane@example.com", "existing_hash");
        String newPhone = "+254700112233";

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Act
        User updatedUser = userService.updatePhoneNumber(userId, newPhone);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("+254700112233", updatedUser.getPhoneNumber());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("Should throw exception when updating phone number for non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.updatePhoneNumber(nonExistentId, "+254700112233")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with invalid phone number")
    void shouldThrowExceptionWhenUpdatingWithInvalidPhoneNumber() {
        UUID userId = UUID.randomUUID();
        User existingUser = new User(userId, "Jane Doe", "jane@example.com", "existing_hash");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class, () ->
                userService.updatePhoneNumber(userId, "bad")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should remove phone number successfully")
    void shouldRemovePhoneNumberSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = new User(userId, "Jane Doe", "jane@example.com", "+254712345678", "hash");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Act
        User updatedUser = userService.removePhoneNumber(userId);

        // Assert
        assertNotNull(updatedUser);
        assertNull(updatedUser.getPhoneNumber());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("Should find user by phone number")
    void shouldFindUserByPhoneNumber() {
        String phone = "+254712345678";
        User user = new User("Jane Doe", "jane@example.com", phone, "hash");

        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByPhoneNumber(phone);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findByPhoneNumber(phone);
    }

    @Test
    @DisplayName("Should return empty when user not found by phone number")
    void shouldReturnEmptyWhenUserNotFoundByPhone() {
        String phone = "+254700000000";
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByPhoneNumber(phone);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByPhoneNumber(phone);
    }

    @Test
    @DisplayName("Should return empty when searching by null or blank phone number")
    void shouldReturnEmptyWhenSearchingByNullOrBlankPhone() {
        Optional<User> resultNull = userService.findByPhoneNumber(null);
        Optional<User> resultBlank = userService.findByPhoneNumber("   ");

        assertFalse(resultNull.isPresent());
        assertFalse(resultBlank.isPresent());
        verify(userRepository, never()).findByPhoneNumber(any());
    }

    @Test
    @DisplayName("Should check if phone number exists")
    void shouldCheckIfPhoneNumberExists() {
        String phone = "+254712345678";
        when(userRepository.existsByPhoneNumber(phone)).thenReturn(true);

        assertTrue(userService.existsByPhoneNumber(phone));
        assertFalse(userService.existsByPhoneNumber(null));
        assertFalse(userService.existsByPhoneNumber("  "));

        verify(userRepository, times(1)).existsByPhoneNumber(phone);
    }
}
