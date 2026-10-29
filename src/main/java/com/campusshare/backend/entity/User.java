package com.campusshare.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
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
}