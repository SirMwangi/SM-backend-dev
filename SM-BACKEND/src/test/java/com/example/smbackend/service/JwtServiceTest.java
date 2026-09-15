package com.example.smbackend.service;

import com.example.smbackend.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Logic Checkpoint — JwtService unit tests.
 *
 * <p>Uses Mockito's JUnit 5 extension for lightweight instantiation.
 * No Spring context is started; {@code @Value} fields are injected via
 * {@link ReflectionTestUtils#setField} so the service can be exercised in
 * complete isolation from infrastructure.
 *
 * <p>Test JWT secret is a Base64-encoded 256-bit key safe for HS256.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    /**
     * Base64-encoded 256-bit key — safe for HS256.
     * Decoded: "test-secret-key-for-sm-backend-unit-tests!!!"
     */
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci1zbS1iYWNrZW5kLXVuaXQtdGVzdHMhISE=";

    /** 1 hour expressed in milliseconds. */
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtService jwtService;

    /** Builds a real {@link JwtService} instance with injected test properties. */
    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", EXPIRATION_MS);
    }

    // -------------------------------------------------------------------------
    // Helper — builds a minimal User that acts as a UserDetails principal
    // -------------------------------------------------------------------------

    private User buildTestUser(String email) {
        return new User(UUID.randomUUID(), "Test User", email, "+254711000000", "hashed_pw");
    }

    // -------------------------------------------------------------------------
    // generateToken tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generateToken() should return a non-null, non-blank JWT string")
    void generateToken_shouldReturnNonBlankString() {
        User user = buildTestUser("alice@example.com");

        String token = jwtService.generateToken(user);

        assertThat(token)
                .as("Generated token must not be null or blank")
                .isNotNull()
                .isNotBlank();
    }

    @Test
    @DisplayName("generateToken() should produce a token with three JWT segments (header.payload.signature)")
    void generateToken_shouldHaveThreeSegments() {
        User user = buildTestUser("bob@example.com");

        String token = jwtService.generateToken(user);
        String[] parts = token.split("\\.");

        assertThat(parts)
                .as("JWT must consist of exactly 3 dot-separated segments")
                .hasSize(3);
    }

    // -------------------------------------------------------------------------
    // extractUsername tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("extractUsername() should retrieve the exact email used during token generation")
    void extractUsername_shouldReturnCorrectEmail() {
        String expectedEmail = "carol@example.com";
        User user = buildTestUser(expectedEmail);

        String token = jwtService.generateToken(user);
        String extractedEmail = jwtService.extractUsername(token);

        assertThat(extractedEmail)
                .as("Extracted username must equal the email passed to generateToken()")
                .isEqualTo(expectedEmail);
    }

    @Test
    @DisplayName("extractUsername() should distinguish between two different users' tokens")
    void extractUsername_shouldDistinguishBetweenUsers() {
        User userA = buildTestUser("alice@example.com");
        User userB = buildTestUser("bob@example.com");

        String tokenA = jwtService.generateToken(userA);
        String tokenB = jwtService.generateToken(userB);

        assertThat(jwtService.extractUsername(tokenA)).isEqualTo("alice@example.com");
        assertThat(jwtService.extractUsername(tokenB)).isEqualTo("bob@example.com");
        assertThat(tokenA).isNotEqualTo(tokenB);
    }

    // -------------------------------------------------------------------------
    // isTokenValid tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("isTokenValid() should return true for a freshly generated token")
    void isTokenValid_shouldReturnTrueForFreshToken() {
        User user = buildTestUser("dave@example.com");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user))
                .as("A just-generated token must be valid for its owner")
                .isTrue();
    }

    @Test
    @DisplayName("isTokenValid() should return false when token belongs to a different user")
    void isTokenValid_shouldReturnFalseForWrongUser() {
        User owner = buildTestUser("owner@example.com");
        User impostor = buildTestUser("impostor@example.com");

        String token = jwtService.generateToken(owner);

        assertThat(jwtService.isTokenValid(token, impostor))
                .as("Token issued for owner must not validate for a different user")
                .isFalse();
    }

    @Test
    @DisplayName("isTokenValid() should return false (or throw ExpiredJwtException) for an expired token")
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        // Create a service instance with an already-expired window (-1 ms)
        JwtService expiredJwtService = new JwtService();
        ReflectionTestUtils.setField(expiredJwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredJwtService, "jwtExpirationMs", -1L);

        User user = buildTestUser("expired@example.com");
        String token = expiredJwtService.generateToken(user);

        // JJWT 0.12.x throws ExpiredJwtException at parse time when a token is past its
        // expiration window.  isTokenValid() propagates this via extractUsername() before
        // even reaching the expiration comparison — so we accept EITHER:
        //   a) isTokenValid() returns false (if JwtService catches the exception internally), OR
        //   b) isTokenValid() throws ExpiredJwtException (if the exception propagates)
        // Both outcomes correctly indicate that the token is not valid.
        boolean valid;
        try {
            valid = jwtService.isTokenValid(token, user);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // ExpiredJwtException thrown = token correctly rejected as expired
            return;
        }
        assertThat(valid)
                .as("Token with past expiration must be rejected")
                .isFalse();
    }

    // -------------------------------------------------------------------------
    // Invalid token rejection
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("extractUsername() should throw for a malformed token")
    void extractUsername_shouldThrowForMalformedToken() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.valid.jwt"))
                .as("Malformed JWT must cause an exception during parsing")
                .isInstanceOf(Exception.class);
    }
}
