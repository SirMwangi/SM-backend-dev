package com.example.smbackend;

import com.example.smbackend.config.ApplicationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security filter chain configuration.
 *
 * <p>Enforces a fully stateless, JWT-based security model:
 * <ul>
 *   <li>CSRF protection is disabled (not needed for stateless APIs).</li>
 *   <li>Sessions are never created or consulted.</li>
 *   <li>The custom {@link AuthenticationProvider} from {@link ApplicationConfig}
 *       is registered so that {@code DaoAuthenticationProvider} handles credential checks.</li>
 *   <li>Auth endpoints are public; everything else requires a valid JWT.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
