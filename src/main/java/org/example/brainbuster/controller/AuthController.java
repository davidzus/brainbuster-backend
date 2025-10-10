package org.example.brainbuster.controller;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.dto.auth.AuthResponse;
import org.example.brainbuster.dto.auth.LoginRequest;
import org.example.brainbuster.dto.user.UserRequest;
import org.example.brainbuster.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRequest userRequest) {
        AuthResponse response = authService.register(userRequest);

        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);

        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String username = request.get("username");
        boolean isValid = authService.validateToken(token, username);
        return ResponseEntity.ok(isValid);
    }
}