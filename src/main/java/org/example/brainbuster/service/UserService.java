package org.example.brainbuster.service;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.dto.user.UserRequest;
import org.example.brainbuster.dto.user.UserResponse;
import org.example.brainbuster.exception.UserNotFoundException;
import org.example.brainbuster.model.User;
import org.example.brainbuster.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
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

    public UserResponse createUser(UserRequest userRequest) {
        User user = toEntity(userRequest);
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public UserResponse createAdmin(UserRequest userRequest) {
        User user = toEntity(userRequest);
        user.setRole("admin");
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse).toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toUserResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        User user = toEntity(userRequest);
        user.setId(id);
        User updatedUser = userRepository.save(user);
        return toUserResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }

    private User toEntity(UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole("user");
        user.setHighScore(0);
        return user;
    }
}