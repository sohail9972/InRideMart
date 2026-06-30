package com.inridemart.auth.application;

import com.inridemart.auth.domain.Role;

import java.util.UUID;

public record AuthResult(UUID userId, String email, Role role, String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
}

