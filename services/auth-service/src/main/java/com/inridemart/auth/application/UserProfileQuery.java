package com.inridemart.auth.application;

import com.inridemart.auth.domain.Role;

import java.util.UUID;

public record UserProfileQuery(UUID id, String email, Role role) {
}

