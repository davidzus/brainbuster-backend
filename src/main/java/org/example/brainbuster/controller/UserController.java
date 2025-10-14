package org.example.brainbuster.controller;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.dto.user.UserRequest;
import org.example.brainbuster.dto.user.UserResponse;
import org.example.brainbuster.dto.user.UserHighscore;
import org.example.brainbuster.service.JwtService;
import org.example.brainbuster.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtService jwtServcie;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getCurrentUser")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userService.getUserByUsername(principal.getUsername()));
    }

    @GetMapping("/userhighscore")
    public ResponseEntity<UserHighscore> getUserHighscore(@AuthenticationPrincipal UserDetails principal) {
        String username = principal.getUsername();
        return ResponseEntity.ok(userService.getHighscore(username));
    }

    @GetMapping("/highscores")
    public ResponseEntity<List<UserHighscore>> getAllHighscores() {
        return ResponseEntity.ok(userService.getAllHighscores());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        UserResponse created = userService.createUser(userRequest);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") Long id, @RequestBody UserRequest userRequest) {
        try {
            UserResponse updatedUser = userService.updateUser(id, userRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}