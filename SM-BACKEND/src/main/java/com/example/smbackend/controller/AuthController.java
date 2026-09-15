package com.example.smbackend.controller;

import com.example.smbackend.domain.User;
import com.example.smbackend.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

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
}
