package com.example.expensemanager.security;

import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.expensemanager.model.User;
import com.example.expensemanager.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final UserRepository repo;
    private final long expirationMs;
    private static final HexFormat HEX = HexFormat.of();

    public JwtUtil(UserRepository repo, @Value("${jwt.expirationMs}") long expirationMs) {
        this.repo = repo;
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        User user = repo.findByUsername(username)
            .orElseThrow(() -> new JwtException("User not found"));
        byte[] keyBytes = HEX.parseHex(user.getJwtSecret());
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        Instant now = Instant.now();

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusMillis(expirationMs)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    private Claims parseToken(String token) {
        String subject = Jwts.parserBuilder()
            .build()
            .parseClaimsJwt(getUnsignedToken(token))
            .getBody()
            .getSubject();

        User user = repo.findByUsername(subject)
            .orElseThrow(() -> new JwtException("User not found"));

        byte[] keyBytes = HEX.parseHex(user.getJwtSecret());
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String getUnsignedToken(String jwt) {
        int lastDot = jwt.lastIndexOf('.');
        if (lastDot < 0) throw new JwtException("Invalid token format");
        return jwt.substring(0, lastDot + 1);
    }
}