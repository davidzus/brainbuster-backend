package org.example.brainbuster.service;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.dto.user.UserRequest;
import org.example.brainbuster.dto.user.UserResponse;
import org.example.brainbuster.model.User;
import org.example.brainbuster.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getHighScore(),
            user.getCreatedAt()
        );
    }

    private User toEntity(UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(userRequest.getRole() != null ? userRequest.getRole() : "user");
        user.setHighScore(0);
        return user;
    }

    public UserResponse createUser(UserRequest userRequest) {
        User user = toEntity(userRequest);
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toUserResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        User user = toEntity(userRequest);
        user.setId(id);
        User updatedUser = userRepository.save(user);
        return toUserResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
}