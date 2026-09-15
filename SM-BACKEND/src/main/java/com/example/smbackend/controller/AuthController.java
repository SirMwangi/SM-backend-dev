package com.example.smbackend.controller;

import com.example.smbackend.domain.User;
import com.example.smbackend.service.JwtService;
import com.example.smbackend.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that handles user authentication (registration and login).
 *
 * <p>Public endpoints — no JWT required:
 * <ul>
 *   <li>{@code POST /api/auth/register} — create a new user account</li>
 *   <li>{@code POST /api/auth/login}    — authenticate and receive a JWT</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Constructor-injected dependencies.
     *
     * @param userService            manages user lifecycle (creation, lookup)
     * @param authenticationManager  Spring Security manager that validates credentials
     * @param jwtService             issues and validates JSON Web Tokens
     */
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // -------------------------------------------------------------------------
    // Endpoints
    // -------------------------------------------------------------------------

    /**
     * Registers a new user account.
     *
     * @param request registration payload containing name, email, password, and optional phone number
     * @return HTTP 201 CREATED with the persisted {@link User} entity
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        String password = request.getPassword() != null ? request.getPassword() : request.getRawPassword();
        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                password
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Authenticates a user with email and password, returning a signed JWT on success.
     *
     * <p>Delegates credential verification to {@link AuthenticationManager}, which in
     * turn uses {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
     * backed by the database. Throws {@link org.springframework.security.core.AuthenticationException}
     * (resulting in HTTP 401) if authentication fails.
     *
     * @param request login payload containing email and password
     * @return HTTP 200 OK with an {@link AuthResponse} containing the signed JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    /**
     * Handles failed authentication attempts (wrong email or password).
     *
     * <p>Spring Security's {@link org.springframework.security.core.AuthenticationException}
     * subclasses (e.g. {@link org.springframework.security.authentication.BadCredentialsException})
     * thrown inside a {@code @RestController} method are not intercepted by
     * {@code ExceptionTranslationFilter} on {@code permitAll()} paths, so we map them
     * explicitly to HTTP 401 Unauthorized here.
     *
     * @param ex the authentication exception
     * @return HTTP 401 with an error detail body
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(
            org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<java.util.Map<String, String>> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(java.util.Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Unauthorized"));
    }

    // -------------------------------------------------------------------------
    // Request / Response DTOs (static inner classes)
    // -------------------------------------------------------------------------

    /**
     * Payload for the {@code POST /api/auth/register} endpoint.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String rawPassword;
        private String phoneNumber;

        public RegisterRequest() {
        }

        public RegisterRequest(String name, String email, String password, String phoneNumber) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password != null ? password : rawPassword;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRawPassword() {
            return rawPassword != null ? rawPassword : password;
        }

        public void setRawPassword(String rawPassword) {
            this.rawPassword = rawPassword;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    /**
     * Payload for the {@code POST /api/auth/login} endpoint.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {
        }

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Response body for the {@code POST /api/auth/login} endpoint.
     */
    public static class AuthResponse {
        private final String token;

        public AuthResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
