package com.example.learn.Service;

import com.example.learn.Entity.User;
import com.example.learn.Repository.UserRepository;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Same Secret Key Encoding as JwtUtil
    private final String secretKeyString = "cXhPNTF6YjFqTmczcXJXZlU4anNLODZlOTRtdURwOVFhLw=="; // Same Key used in JwtUtil
    private final SecretKey secretKey = new SecretKeySpec(secretKeyString.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());

    private final ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lockoutTime = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    @Autowired
    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, String> loginAndGenerateToken(String email, String rawPassword) {
        Map<String, String> response = new HashMap<>();

        if (!userRepository.existsByEmail(email)) {
            response.put("status", "error");
            response.put("message", "Email does not exist");
            return response;
        }

        if (isLocked(email)) {
            response.put("status", "error");
            response.put("message", "Account is locked. Try again later.");
            return response;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        User user = userOpt.get();

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            resetAttempts(email);
            String role = user.getRole().name();
            String token = generateToken(email, role);

            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("role", role);

            // Role-based redirection logic
            switch (role.toUpperCase()) {
                case "TRAINER" -> response.put("redirect", "/trainer-dashboard.html");
                case "STUDENT" -> response.put("redirect", "/student-dashboard.html");
                default -> response.put("redirect", "/dashboard");
            }
        } else {
            increaseAttempts(email);
            response.put("status", "error");
            response.put("message", "Invalid password");
        }

        return response;
    }

    public Map<String, String> validateTokenFromCookie(String token) {
        Map<String, String> response = new HashMap<>();
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            String email = claimsJws.getBody().getSubject();
            String role = (String) claimsJws.getBody().get("role");

            response.put("status", "success");
            response.put("email", email);
            response.put("role", role);

        } catch (JwtException e) {
            response.put("status", "error");
            response.put("message", "Invalid or expired token");
        }
        return response;
    }

    private String generateToken(String email, String role) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 1000 * 60 * 60; // 1 hour

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expMillis))
                .signWith(secretKey)
                .compact();
    }

    private void increaseAttempts(String email) {
        failedAttempts.merge(email, 1, Integer::sum);
        if (failedAttempts.get(email) >= MAX_ATTEMPTS) {
            lockoutTime.put(email, LocalDateTime.now());
        }
    }

    private void resetAttempts(String email) {
        failedAttempts.remove(email);
        lockoutTime.remove(email);
    }

    private boolean isLocked(String email) {
        if (!lockoutTime.containsKey(email)) return false;

        LocalDateTime lockTime = lockoutTime.get(email);
        if (lockTime.plusMinutes(LOCK_DURATION_MINUTES).isAfter(LocalDateTime.now())) {
            return true;
        } else {
            lockoutTime.remove(email);
            failedAttempts.remove(email);
            return false;
        }
    }
} 