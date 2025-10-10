package org.example.brainbuster.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.brainbuster.dto.user.UserResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponse user;
    private String message;
}