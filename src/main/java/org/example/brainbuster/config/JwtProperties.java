package org.example.brainbuster.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "mySuperSecretKeyForBrainBusterJWT2025SecureEnough";
    private long expiration = 86400000; // 24 hours
    private long refreshExpiration = 604800000; // 7 days
}