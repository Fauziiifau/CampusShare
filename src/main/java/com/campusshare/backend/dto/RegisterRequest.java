package com.campusshare.backend.dto;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;

    // Explicit JavaBean accessors: no Lombok annotation processing needed.
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
}