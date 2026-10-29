package com.campusshare.backend.security;

import com.campusshare.backend.entity.User;
import com.campusshare.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Component

public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {
    // Constructor injection is explicit to avoid Lombok-related compilation issues.
    public GoogleLoginSuccessHandler(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            response.sendRedirect("/login?google_error=invalid");
            return;
        }
        // Email has to be verified by Google. Never trust arbitrary unverified email claims.
        if (!Boolean.TRUE.equals(oidcUser.getClaim("email_verified"))) {
            response.sendRedirect("/login?google_error=unverified");
            return;
        }
        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            response.sendRedirect("/login?google_error=invalid");
            return;
        }
        email = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByGoogleId(subject).orElse(null);
        if (user == null) {
            // Don't silently auto-link an existing LOCAL account by email.
            if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
                response.sendRedirect("/login?google_error=account_exists");
                return;
            }
            user = new User();
            user.setEmail(email);
            user.setUsername(generateUniqueUsername(email));
            user.setAuthProvider("GOOGLE");
            user.setGoogleId(subject);
            user.setRole("MAHASISWA");
            // Keep the existing non-null password column; this is NOT a usable password.
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString() + UUID.randomUUID()));
            try {
                user = userRepository.saveAndFlush(user);
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                // A concurrent sign-in could have created the same user.
                User existing = userRepository.findByGoogleId(subject).orElse(null);
                if (existing == null) {
                    response.sendRedirect("/login?google_error=account_exists");
                    return;
                }
                user = existing;
            }
        }
        if (!"GOOGLE".equals(user.getAuthProvider()) || !subject.equals(user.getGoogleId())) {
            response.sendRedirect("/login?google_error=invalid");
            return;
        }
        // OAuth's temporary session is no longer necessary after the exchange.
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        String token = jwtUtils.generateToken(user.getEmail());
        // Existing CampusShare uses Bearer JWT saved in localStorage. Send it in the response
        // HTML, not in a URL (which would expose it via history, logs and referrers).
        // This transitional approach preserves the existing dashboard without changing all APIs.
        byte[] nonceBytes = new byte[20];
        RNG.nextBytes(nonceBytes);
        String nonce = Base64.getEncoder().encodeToString(nonceBytes);
        String encodedEmail = Base64.getEncoder().encodeToString(user.getEmail().getBytes(StandardCharsets.UTF_8));
        String encodedRole = Base64.getEncoder().encodeToString(user.getRole().getBytes(StandardCharsets.UTF_8));
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", "default-src 'none'; script-src 'nonce-" + nonce + "'; base-uri 'none'; frame-ancestors 'none'");
        String html = "<!doctype html><html lang='id'><head><meta charset='utf-8'><title>Masuk ke CampusShare</title></head>"
            + "<body><p>Sedang menyelesaikan login Google...</p><script nonce='" + nonce + "'>"
            + "const decode=s=>new TextDecoder().decode(Uint8Array.from(atob(s),c=>c.charCodeAt(0)));"
            + "localStorage.setItem('token','" + token + "');"
            + "localStorage.setItem('email',decode('" + encodedEmail + "'));"
            + "localStorage.setItem('role',decode('" + encodedRole + "'));"
            + "location.replace('/dashboard');</script></body></html>";
        response.getWriter().write(html);
    }

    private String generateUniqueUsername(String email) {
        String stem = email.substring(0, email.indexOf('@'))
            .replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase(Locale.ROOT);
        if (stem.isBlank()) stem = "mahasiswa";
        if (stem.length() > 35) stem = stem.substring(0, 35);
        String candidate = stem;
        int tries = 0;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = stem + "_" + UUID.randomUUID().toString().substring(0, 8);
            tries++;
            if (tries > 12) throw new IllegalStateException("Tidak dapat membuat nama pengguna unik");
        }
        return candidate;
    }
}