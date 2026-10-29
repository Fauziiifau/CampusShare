package com.campusshare.backend.controller;

import com.campusshare.backend.dto.LoginRequest;
import com.campusshare.backend.dto.RegisterRequest;
import com.campusshare.backend.entity.User;
import com.campusshare.backend.repository.UserRepository;
import com.campusshare.backend.security.JwtUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")

public class AuthController {
    // Constructor injection is explicit to avoid Lombok-related compilation issues.
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (request == null || request.getUsername() == null || request.getEmail() == null
            || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Nama pengguna, email, dan password wajib diisi.");
        }
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String password = request.getPassword();

        if (username.length() < 3 || username.length() > 50) {
            return ResponseEntity.badRequest().body("Nama pengguna harus 3–50 karakter.");
        }
        if (username.contains("@")) {
            return ResponseEntity.badRequest().body("Nama pengguna tidak boleh mengandung simbol @.");
        }
        if (email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().body("Masukkan alamat email yang valid.");
        }
        if (password.length() < 8 || password.length() > 128) {
            return ResponseEntity.badRequest().body("Password harus 8–128 karakter.");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email sudah terdaftar. Silakan masuk.");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nama pengguna sudah dipakai. Coba nama lain.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole("MAHASISWA");
        user.setAuthProvider("LOCAL");
        user.setPassword(passwordEncoder.encode(password));
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Email atau nama pengguna sudah digunakan. Silakan coba lagi.");
        }
        return ResponseEntity.ok("Registrasi berhasil! Silakan masuk.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Email dan password wajib diisi.");
        }
        String identifier = request.getEmail().trim();
        User user = identifier.contains("@")
            ? userRepository.findByEmailIgnoreCase(identifier.toLowerCase(Locale.ROOT)).orElse(null)
            : userRepository.findByUsernameIgnoreCase(identifier).orElse(null);
        // Same response for absent users, wrong passwords, and Google-only accounts.
        // This avoids revealing whether an account exists.
        if (user == null || "GOOGLE".equals(user.getAuthProvider()) ||
            !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Email atau password salah. Jika mendaftar lewat Google, gunakan tombol Google.");
        }
        String token = jwtUtils.generateToken(user.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("message", "Login berhasil!");
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }
}