package com.ats.controller;

import com.ats.model.User;
import com.ats.service.UserService;
import com.ats.dto.LoginRequest;
import com.ats.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            if (userService.getUserByUsername(user.getUsername()) != null) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            if (userService.getUserByEmail(user.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            
            User savedUser = userService.createUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Signup failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            if (user != null) {
                String token = generateToken(user); // In production, use proper JWT
                LoginResponse response = new LoginResponse(token, user.getRole().toString());
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    private String generateToken(User user) {
        // In production, implement proper JWT token generation
        return "dummy-token-" + user.getId() + "-" + System.currentTimeMillis();

        // return Jwts.builder()
        //         .setSubject(user.getUsername()) // or user.getId() if preferred
        //         .claim("userId", user.getId())
        //         .claim("email", user.getEmail()) // optional
        //         .setIssuedAt(new Date())
        //         .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        //         .signWith(SECRET_KEY)
        //         .compact();
    }
} 