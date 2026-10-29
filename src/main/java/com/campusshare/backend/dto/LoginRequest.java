package com.campusshare.backend.dto;

public class LoginRequest {
    private String email;
    private String password;

    // Explicit JavaBean accessors: no Lombok annotation processing needed.
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
}