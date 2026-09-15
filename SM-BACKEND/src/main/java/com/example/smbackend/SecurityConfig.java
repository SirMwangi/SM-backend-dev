package com.example.smbackend;

import com.example.smbackend.domain.User;
import com.example.smbackend.repository.UserRepository;
import com.example.smbackend.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration for SM-BACKEND.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Stateless JWT-based session management</li>
 *   <li>Public endpoints: {@code /api/auth/**} and {@code /error}</li>
 *   <li>Exposes {@link AuthenticationManager} and {@link AuthenticationProvider} beans</li>
 *   <li>Wires the custom {@link JwtAuthenticationFilter} into the filter chain</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public SecurityConfig(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // -------------------------------------------------------------------------
    // Security filter chain
    // -------------------------------------------------------------------------

    /**
     * Configures the HTTP security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled (stateless REST API uses JWTs)</li>
     *   <li>Session policy: STATELESS</li>
     *   <li>Public paths: {@code /api/auth/**}, {@code /error}</li>
     *   <li>JWT filter inserted before the standard username/password filter</li>
     * </ul>
     *
     * @param http                   Spring Security's HTTP security builder
     * @param jwtAuthenticationFilter the JWT request filter bean
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                // Return HTTP 401 (not 403) when authentication is required but absent/invalid
                .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(
                                jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED,
                                authException.getMessage()))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Authentication beans
    // -------------------------------------------------------------------------

    /**
     * Loads a {@link User} by email. Used by {@link DaoAuthenticationProvider} during login.
     *
     * @return {@link UserDetailsService} that queries the database by email
     * @throws UsernameNotFoundException if no user with that email exists
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No user found with email: " + username));
    }

    /**
     * Wires the {@link UserDetailsService} and {@link PasswordEncoder} into a DAO provider.
     *
     * @return configured {@link DaoAuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} so it can be injected into
     * {@link com.example.smbackend.controller.AuthController} for programmatic login.
     *
     * @param config Spring's {@link AuthenticationConfiguration}
     * @return the application-level {@link AuthenticationManager}
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder — shared across the application context.
     *
     * @return {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
