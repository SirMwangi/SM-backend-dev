package com.example.smbackend.controller;

import com.example.smbackend.domain.User;
import com.example.smbackend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Network Checkpoint — AuthController integration tests.
 *
 * <p>Uses {@code @SpringBootTest} with the H2 in-memory datasource (configured in
 * {@code src/test/resources/application.yml}) and {@link MockMvc} to exercise the
 * full Spring Security + MVC pipeline without starting an actual HTTP server.
 *
 * <p>{@link AuthenticationManager} and {@link JwtService} are replaced with Mockito
 * mocks via {@code @MockitoBean} so the test focuses purely on the routing contract
 * and response shape, independent of real database state.
 *
 * <p>Note: {@code @WebMvcTest} was removed in Spring Boot 4.0.  The equivalent is
 * {@code @SpringBootTest} + {@code MockMvcBuilders.webAppContextSetup()} with
 * {@code springSecurity()} applied.
 */
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    /** Builds MockMvc fresh for every test so mock state is clean. */
    private MockMvc mockMvc() {
        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    // -------------------------------------------------------------------------
    // Login — happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns HTTP 200 and a JWT token")
    void login_withValidCredentials_returns200AndToken() throws Exception {
        // Arrange
        String email = "frank@example.com";
        String rawPassword = "securePassword!";
        String expectedToken = "mocked.jwt.token";

        User authenticatedUser = new User(
                UUID.randomUUID(), "Frank", email, "+254711000001", "hashed_pw");

        Authentication successfulAuth =
                new UsernamePasswordAuthenticationToken(authenticatedUser, null,
                        authenticatedUser.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuth);
        when(jwtService.generateToken(authenticatedUser)).thenReturn(expectedToken);

        // Act & Assert
        mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, rawPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(expectedToken));
    }

    // -------------------------------------------------------------------------
    // Login — sad paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/login with wrong password returns HTTP 401")
    void login_withBadCredentials_returns401() throws Exception {
        // Arrange: AuthenticationManager throws on bad credentials
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "bad@example.com",
                                  "password": "wrongPassword"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login with null credentials returns HTTP 401")
    void login_withNullCredentials_returns401() throws Exception {
        // Arrange: null email/password also triggers authentication failure
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
