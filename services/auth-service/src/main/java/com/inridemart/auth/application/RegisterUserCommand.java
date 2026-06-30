package com.inridemart.auth.application;

import com.inridemart.auth.domain.Role;

public record RegisterUserCommand(String email, String password, Role role) {
}

