package com.campusshare.backend.controller;

import com.campusshare.backend.dto.LoginRequest;
import com.campusshare.backend.dto.RegisterRequest;
import com.campusshare.backend.entity.User;
import com.campusshare.backend.repository.UserRepository;
import com.campusshare.backend.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email sudah terdaftar!");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole("MAHASISWA");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Registrasi berhasil!");
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Password salah!");
        }
        String token = jwtUtils.generateToken(user.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login Berhasil!");
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }
}