package com.inridemart.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String email, Role role, Instant occurredAt) {
}

