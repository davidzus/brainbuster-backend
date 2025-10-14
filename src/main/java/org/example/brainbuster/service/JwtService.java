package org.example.brainbuster.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.example.brainbuster.config.JwtProperties;
import org.example.brainbuster.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ACCESS_PREFIX = "access";

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().startsWith(ROLE_PREFIX) ? user.getRole() : ROLE_PREFIX + user.getRole())
                .claim("type", ACCESS_PREFIX)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().startsWith(ROLE_PREFIX) ? user.getRole() : ROLE_PREFIX + user.getRole())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        Claims c = extractAllClaims(token);
        boolean notExpired = c.getExpiration() != null && c.getExpiration().after(new Date());
        boolean subjectMatches = user.getUsername().equals(c.getSubject());
        boolean accessType = ACCESS_PREFIX.equals(c.get("type", String.class));
        return notExpired && subjectMatches && accessType;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isAccessTokenValid(String token, UserDetails user) {
        Claims c = extractAllClaims(token);
        return c.getExpiration().after(new Date())
                && user.getUsername().equals(c.getSubject())
                && ACCESS_PREFIX.equals(c.get("type", String.class));
    }

    public boolean isRefreshTokenValid(String token, String username) {
        Claims c = extractAllClaims(token);
        return c.getExpiration().after(new Date())
                && username.equals(c.getSubject())
                && "refresh".equals(c.get("type", String.class));
    }
}
