package com.inridemart.auth.adapters.in.web;

import com.inridemart.auth.domain.Role;

import java.util.UUID;

public record UserProfileResponse(UUID id, String email, Role role) {
}

