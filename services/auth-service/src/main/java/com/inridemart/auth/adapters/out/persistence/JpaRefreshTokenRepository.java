package com.inridemart.auth.adapters.out.persistence;

import com.inridemart.auth.domain.ports.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {
    private final SpringDataRefreshTokenRepository repository;

    public JpaRefreshTokenRepository(SpringDataRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant createdAt) {
        repository.save(new RefreshTokenEntity(id, userId, tokenHash, expiresAt, createdAt));
    }
}

