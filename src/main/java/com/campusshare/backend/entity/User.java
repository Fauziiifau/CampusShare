package com.campusshare.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash; NEVER store a raw Google password.

    private String role;

    // Nullable so pre-existing user rows remain compatible with ddl-auto=update.
    @Column(name = "auth_provider")
    private String authProvider = "LOCAL";

    @Column(name = "google_id", unique = true)
    private String googleId;

    // Explicit JavaBean accessors: no Lombok annotation processing needed.
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
    public String getAuthProvider() { return this.authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getGoogleId() { return this.googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
}