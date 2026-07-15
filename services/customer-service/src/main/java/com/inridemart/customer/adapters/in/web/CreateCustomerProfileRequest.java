package com.inridemart.customer.adapters.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCustomerProfileRequest(
        UUID userId,
        @NotBlank @Size(min = 2, max = 150) String fullName,
        @Size(max = 30) String phoneNumber,
        @Valid AddressRequest address
) {
}
