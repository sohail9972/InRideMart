package com.inridemart.auth.adapters.in.web;

import com.inridemart.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 10, max = 128) String password,
        Role role
) {
}

