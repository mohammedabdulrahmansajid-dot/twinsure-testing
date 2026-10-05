package org.example.identityservice.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.identityservice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final String secret;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {

        this.secret = secret;
        this.expiration = expiration;
    }

    public String generateToken(User user) {

        Date issuedAt = new Date();

        Date expirationDate =
                new Date(
                        issuedAt.getTime() + expiration
                );

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("role", user.getRole().name())
                .claim("customerId", user.getCustomerId())
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    public Long extractUserId(String token) {

        Number userId =
                extractAllClaims(token)
                        .get("userId", Number.class);

        return userId.longValue();
    }

    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }

    public Long extractCustomerId(String token) {

        Number customerId =
                extractAllClaims(token)
                        .get("customerId", Number.class);

        if (customerId == null) {
            return null;
        }

        return customerId.longValue();
    }

    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractAllClaims(token);

            return claims.getExpiration()
                    .after(new Date());

        } catch (Exception exception) {

            return false;
        }
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes =
                secret.getBytes(
                        StandardCharsets.UTF_8
                );

        return Keys.hmacShaKeyFor(keyBytes);
    }
}