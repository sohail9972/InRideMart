package com.inridemart.auth.config;

import com.inridemart.auth.domain.UserAccount;
import com.inridemart.auth.domain.ports.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService implements TokenService {
    private final JwtProperties properties;
    private final Clock clock;
    private final SecretKey key;

    public JwtTokenService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issueAccessToken(UserAccount user) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(Duration.ofMinutes(properties.accessTokenTtlMinutes()));
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.id().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claims(Map.of("email", user.email().value(), "role", user.role().name(), "token_use", "access"))
                .signWith(key)
                .compact();
    }

    @Override
    public String issueRefreshToken(UUID tokenId) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(Duration.ofDays(properties.refreshTokenTtlDays()));
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(tokenId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("token_use", "refresh")
                .signWith(key)
                .compact();
    }

    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
