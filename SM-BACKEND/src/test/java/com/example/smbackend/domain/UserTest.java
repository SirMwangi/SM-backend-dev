package com.example.smbackend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    @DisplayName("Should create user with default constructor and setters")
    void shouldCreateUserWithDefaultConstructorAndSetters() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("hashed_pw");
        user.setPhoneNumber("+254711223344");

        assertEquals(id, user.getId());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("hashed_pw", user.getPasswordHash());
        assertEquals("+254711223344", user.getPhoneNumber());
    }

    @Test
    @DisplayName("Should create user with 4-arg constructor including phone number")
    void shouldCreateUserWith4ArgConstructorIncludingPhoneNumber() {
        User user = new User("Bob", "bob@example.com", "+254722334455", "secret");

        assertNull(user.getId());
        assertEquals("Bob", user.getName());
        assertEquals("bob@example.com", user.getEmail());
        assertEquals("+254722334455", user.getPhoneNumber());
        assertEquals("secret", user.getPasswordHash());
    }

    @Test
    @DisplayName("Should create user with 5-arg constructor including id and phone number")
    void shouldCreateUserWith5ArgConstructorIncludingIdAndPhoneNumber() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Charlie", "charlie@example.com", "+254733445566", "hashed");

        assertEquals(id, user.getId());
        assertEquals("Charlie", user.getName());
        assertEquals("charlie@example.com", user.getEmail());
        assertEquals("+254733445566", user.getPhoneNumber());
        assertEquals("hashed", user.getPasswordHash());
    }

    @Test
    @DisplayName("Should verify equals and hashCode based on identity and email")
    void shouldVerifyEqualsAndHashCode() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        User user1 = new User(id1, "User One", "user1@example.com", "+254711111111", "hash1");
        User user2 = new User(id1, "User One Updated", "user1@example.com", "+254722222222", "hash2");
        User user3 = new User(id2, "User Two", "user2@example.com", "+254733333333", "hash3");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);
    }

    @Test
    @DisplayName("Should include phoneNumber in toString representation")
    void shouldIncludePhoneNumberInToString() {
        User user = new User("Dave", "dave@example.com", "+254744556677", "hash");
        String toStringOutput = user.toString();

        assertTrue(toStringOutput.contains("Dave"));
        assertTrue(toStringOutput.contains("dave@example.com"));
        assertTrue(toStringOutput.contains("+254744556677"));
    }
}
