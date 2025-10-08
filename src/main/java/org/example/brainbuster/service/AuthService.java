package org.example.brainbuster.service;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.dto.auth.AuthResponse;
import org.example.brainbuster.dto.auth.LoginRequest;
import org.example.brainbuster.dto.user.UserRequest;
import org.example.brainbuster.dto.user.UserResponse;
import org.example.brainbuster.model.User;
import org.example.brainbuster.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getHighScore(),
                user.getCreatedAt()
        );
    }

    public AuthResponse register(UserRequest userRequest) {
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            return new AuthResponse(null, null, "Username already exists");
        }

        UserResponse userResponse = userService.createUser(userRequest);
        String token = jwtService.generateToken(userResponse.getUsername());

        return new AuthResponse(token, userResponse, "Registration successful");
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty()) {
            return new AuthResponse(null, null, "Invalid username or password");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            return new AuthResponse(null, null, "Invalid username or password");
        }

        UserResponse userResponse = convertToUserResponse(user);
        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token, userResponse, "Login successful");
    }

    public boolean validateToken(String token, String username) {
        return jwtService.isTokenValid(token, username);
    }

    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }
}