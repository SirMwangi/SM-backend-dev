package com.example.smbackend.config;

import com.example.smbackend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Central Spring Security infrastructure configuration.
 *
 * <p>Defines the three beans required for the JWT authentication flow:
 * <ul>
 *   <li>{@link UserDetailsService} — loads users from the database by email.</li>
 *   <li>{@link AuthenticationProvider} — ties the user-details service to a password encoder.</li>
 *   <li>{@link AuthenticationManager} — the entry point that the login endpoint uses.</li>
 * </ul>
 */
@Configuration
public class ApplicationConfig {

    private final UserRepository userRepository;

    public ApplicationConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a {@link com.example.smbackend.domain.User} by email address.
     *
     * <p>Because {@code User} implements {@link org.springframework.security.core.userdetails.UserDetails}
     * and maps {@code email} → {@code getUsername()}, Spring Security uses this bean
     * transparently during the authentication step.
     *
     * @return a {@link UserDetailsService} backed by {@link UserRepository}
     * @throws UsernameNotFoundException if no user with the given email exists
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + username));
    }

    /**
     * Configures a {@link DaoAuthenticationProvider} that delegates credential
     * verification to {@link #userDetailsService()} and {@link #passwordEncoder()}.
     *
     * @return a fully wired {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes Spring's default {@link AuthenticationManager} as a bean so it can be
     * injected into {@link com.example.smbackend.controller.AuthController}.
     *
     * @param config auto-configured {@link AuthenticationConfiguration}
     * @return the application-wide {@link AuthenticationManager}
     * @throws Exception if the manager cannot be obtained
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder shared across the application.
     *
     * <p>This bean is also picked up by {@link com.example.smbackend.service.UserService}
     * for hashing passwords at registration time.
     *
     * @return a {@link BCryptPasswordEncoder} with default strength (10 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
