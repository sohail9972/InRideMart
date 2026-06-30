package com.inridemart.auth.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class UserAccount {
    private final UUID id;
    private final EmailAddress email;
    private final PasswordHash passwordHash;
    private final Role role;
    private final AccountStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private UserAccount(UUID id, EmailAddress email, PasswordHash passwordHash, Role role, AccountStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static UserAccount register(EmailAddress email, PasswordHash passwordHash, Role role, Instant now) {
        return new UserAccount(UUID.randomUUID(), email, passwordHash, role, AccountStatus.ACTIVE, now, now);
    }

    public static UserAccount rehydrate(UUID id, EmailAddress email, PasswordHash passwordHash, Role role, AccountStatus status, Instant createdAt, Instant updatedAt) {
        return new UserAccount(id, email, passwordHash, role, status, createdAt, updatedAt);
    }

    public boolean canAuthenticate() {
        return status == AccountStatus.ACTIVE;
    }

    public UUID id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public AccountStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}

