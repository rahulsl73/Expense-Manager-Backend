package com.example.expensemanager.security;

import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private static final HexFormat HEX = HexFormat.of();

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String hexSecret,
            @Value("${jwt.expirationMs}") long expirationMs
    ) {
        byte[] keyBytes = HEX.parseHex(hexSecret.trim());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusMillis(expirationMs)))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
