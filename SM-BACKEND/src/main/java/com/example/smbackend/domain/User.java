package com.example.smbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * User domain entity mapped to the 'users' PostgreSQL table.
 *
 * <p>Implements {@link UserDetails} so that Spring Security can use this entity
 * directly as the authenticated principal throughout the security filter chain.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Default no-argument constructor required by JPA.
     */
    public User() {
    }

    /**
     * Parameterized constructor for creating a new User with credentials.
     *
     * @param name         User's display name
     * @param email        User's unique email address
     * @param passwordHash Securely hashed password
     */
    public User(String name, String email, String passwordHash) {
        this(name, email, null, passwordHash);
    }

    /**
     * Parameterized constructor for creating a new User with credentials and phone number.
     *
     * @param name         User's display name
     * @param email        User's unique email address
     * @param phoneNumber  User's contact phone number
     * @param passwordHash Securely hashed password
     */
    public User(String name, String email, String phoneNumber, String passwordHash) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
    }

    /**
     * Full constructor including the UUID identifier.
     *
     * @param id           Unique user identifier
     * @param name         User's display name
     * @param email        User's unique email address
     * @param passwordHash Securely hashed password
     */
    public User(UUID id, String name, String email, String passwordHash) {
        this(id, name, email, null, passwordHash);
    }

    /**
     * Full constructor including the UUID identifier and phone number.
     *
     * @param id           Unique user identifier
     * @param name         User's display name
     * @param email        User's unique email address
     * @param phoneNumber  User's contact phone number
     * @param passwordHash Securely hashed password
     */
    public User(UUID id, String name, String email, String phoneNumber, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UserDetails contract
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns the email address as the Spring Security username.
     * All authentication lookups in {@link com.example.smbackend.config.ApplicationConfig}
     * are keyed on email.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Returns the BCrypt-hashed password stored in {@code password_hash}.
     * Annotated with {@link JsonIgnore} to prevent it from appearing in API responses.
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Grants every user a single {@code ROLE_USER} authority.
     * Extend this method when role-based access control (RBAC) is required.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /** Account expiry is not used — always returns {@code true}. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Account locking is not used — always returns {@code true}. */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** Credential expiry is not used — always returns {@code true}. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Soft-deletion is not used — always returns {@code true}. */
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) || (id == null && Objects.equals(email, user.email));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
