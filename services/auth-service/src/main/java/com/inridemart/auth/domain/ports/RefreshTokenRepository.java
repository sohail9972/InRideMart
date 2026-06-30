package com.inridemart.auth.domain.ports;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenRepository {
    void save(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant createdAt);
}

