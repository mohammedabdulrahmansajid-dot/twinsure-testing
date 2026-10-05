package org.example.insurancepolicyservice.utility;

// Validates JWTs issued by Identity Service and extracts user information.
// Insurance and policy endpoints need these claims for authentication and ownership checks.

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final String secret;

    public JwtUtil(
            @Value("${jwt.secret}") String secret) {

        this.secret = secret;
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

        Number userId = extractAllClaims(token)
                .get("userId", Number.class);

        if (userId == null) {
            return null;
        }

        return userId.longValue();
    }

    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }

    public Long extractCustomerId(String token) {

        Number customerId = extractAllClaims(token)
                .get("customerId", Number.class);

        if (customerId == null) {
            return null;
        }

        return customerId.longValue();
    }

    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractAllClaims(token);

            return claims.getExpiration() != null
                    && claims.getExpiration()
                    .after(new Date());

        } catch (Exception exception) {

            return false;
        }
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes = secret.getBytes(
                StandardCharsets.UTF_8
        );

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
