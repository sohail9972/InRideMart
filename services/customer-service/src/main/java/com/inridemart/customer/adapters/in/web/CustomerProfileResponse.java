package com.inridemart.customer.adapters.in.web;

import com.inridemart.customer.domain.CustomerProfile;

import java.time.Instant;
import java.util.UUID;

public record CustomerProfileResponse(
        UUID id,
        UUID userId,
        String fullName,
        String phoneNumber,
        AddressResponse address,
        Instant createdAt,
        Instant updatedAt
) {
    static CustomerProfileResponse from(CustomerProfile profile) {
        return new CustomerProfileResponse(
                profile.id(),
                profile.userId(),
                profile.fullName(),
                profile.phoneNumber(),
                AddressResponse.from(profile.address()),
                profile.createdAt(),
                profile.updatedAt()
        );
    }
}
