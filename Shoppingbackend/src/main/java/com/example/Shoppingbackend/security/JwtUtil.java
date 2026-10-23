package com.example.Shoppingbackend.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey secretKey =
            Keys.hmacShaKeyFor(
                    "my-shopping-cart-secret-key-12345678901234567890"
                            .getBytes()
            );

    private final long expirationTime =
            1000 * 60 * 60; // 1 hour

    public String generateToken(String email, String role) {

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expirationTime
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token) {

        Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}