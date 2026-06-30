package com.inridemart.auth.adapters.in.web;

import com.inridemart.auth.application.AuthResult;
import com.inridemart.auth.domain.Role;

import java.util.UUID;

public record AuthResponse(UUID userId, String email, Role role, String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
    static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.userId(), result.email(), result.role(), result.accessToken(), result.refreshToken(), result.tokenType(), result.expiresInSeconds());
    }
}

