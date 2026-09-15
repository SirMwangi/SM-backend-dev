package com.example.smbackend.controller;

import com.example.smbackend.domain.User;
import com.example.smbackend.repository.UserRepository;
import com.example.smbackend.service.JwtService;
import com.example.smbackend.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the public authentication surface:
 * <ul>
 *   <li>{@code POST /api/auth/register} — creates a new user account.</li>
 *   <li>{@code POST /api/auth/login}    — authenticates and returns a JWT.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Register
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new user account.
     *
     * <p>The raw password supplied in the request body is hashed by
     * {@link UserService} before persistence — it is never stored in plain text.
     *
     * @param request registration payload (name, email, password, phoneNumber)
     * @return {@code 201 Created} with the persisted {@link User} entity (password omitted)
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

    // ──────────────────────────────────────────────────────────────────────────
    // Login
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and returns a signed JWT.
     *
     * <p>Flow:
     * <ol>
     *   <li>Delegates credential verification to {@link AuthenticationManager}, which
     *       internally calls {@link com.example.smbackend.config.ApplicationConfig#authenticationProvider()}
     *       → BCrypt comparison.</li>
     *   <li>Loads the full {@link User} principal from the database.</li>
     *   <li>Generates a compact JWT via {@link JwtService#generateToken(org.springframework.security.core.userdetails.UserDetails)}.</li>
     *   <li>Returns the token in an {@link AuthResponse} DTO.</li>
     * </ol>
     *
     * <p>Spring Security throws {@link org.springframework.security.core.AuthenticationException}
     * on bad credentials, which the default error handling maps to {@code 401 Unauthorized}.
     *
     * @param request login payload (email + password)
     * @return {@code 200 OK} containing the JWT access token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Step 1: Validate credentials — throws AuthenticationException on failure.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Step 2: Load the verified user principal.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found after authentication: " + request.getEmail()));

        // Step 3: Issue the JWT and return it.
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DTOs
    // ──────────────────────────────────────────────────────────────────────────

    /** Request body for {@code POST /api/auth/register}. */
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

    /** Request body for {@code POST /api/auth/login}. */
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

    /** Response body for {@code POST /api/auth/login}. */
    public static class AuthResponse {
        private final String accessToken;
        private final String tokenType = "Bearer";

        public AuthResponse(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }
    }
}
