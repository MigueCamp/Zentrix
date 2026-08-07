package com.zentrix.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtService(@Value("${zentrix.jwt.secret}") String secret,
                       @Value("${zentrix.jwt.expiration-minutes}") long expirationMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(AuthenticatedUser user) {
        return generateToken(user, expirationMinutes);
    }

    public String generateToken(AuthenticatedUser user, long ttlMinutes) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(user.subject())
                .claim("roles", user.roles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttlMinutes, ChronoUnit.MINUTES)));

        if (user.userId() != null) {
            builder.claim("userId", user.userId());
        }
        if (user.deviceId() != null) {
            builder.claim("deviceId", user.deviceId());
        }
        if (user.companyId() != null) {
            builder.claim("companyId", user.companyId());
        }

        return builder.signWith(signingKey).compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Integer companyId = claims.get("companyId", Integer.class);
        Integer userId = claims.get("userId", Integer.class);
        Integer deviceId = claims.get("deviceId", Integer.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return new AuthenticatedUser(userId, deviceId, companyId, claims.getSubject(), roles);
    }
}
