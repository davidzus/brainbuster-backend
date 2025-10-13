package org.example.brainbuster.controller;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.dto.auth.AuthResponse;
import org.example.brainbuster.dto.auth.LoginRequest;
import org.example.brainbuster.dto.auth.RefreshRequest;
import org.example.brainbuster.dto.auth.ValidateRequest;
import org.example.brainbuster.dto.user.UserRequest;
import org.example.brainbuster.dto.user.UserResponse;
import org.example.brainbuster.model.User;
import org.example.brainbuster.service.AuthService;
import org.example.brainbuster.service.JwtService;
import org.example.brainbuster.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getHighScore(),
                user.getCreatedAt()
        );
    }

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
    public ResponseEntity<Boolean> validateToken(@RequestBody ValidateRequest req) {
        var userDetails = userService.loadUserByUsername(req.getUsername());
        boolean ok = jwtService.isAccessTokenValid(req.getToken(), userDetails);
        return ResponseEntity.ok(ok);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            String username = jwtService.extractUsername(refreshToken);
            if (!jwtService.isRefreshTokenValid(refreshToken, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userService.findByUsername(username);
            String newToken = jwtService.generateToken(user);               // access
            String newRefreshToken = jwtService.generateRefreshToken(user); // refresh

            UserResponse userResponse = convertToUserResponse(user);
            return ResponseEntity.ok(new AuthResponse(newToken, newRefreshToken, userResponse, "Token refreshed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}