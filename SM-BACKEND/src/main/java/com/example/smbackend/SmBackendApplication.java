package com.example.smbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SM-BACKEND Spring Boot application entry point.
 *
 * <p>Infrastructure beans (PasswordEncoder, AuthenticationManager, etc.) are
 * defined in {@link SecurityConfig} to keep this class focused solely on
 * bootstrapping.
 */
@SpringBootApplication
public class SmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmBackendApplication.class, args);
    }
}
