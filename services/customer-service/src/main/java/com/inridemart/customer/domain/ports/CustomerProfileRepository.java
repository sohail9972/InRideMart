package com.inridemart.customer.domain.ports;

import com.inridemart.customer.domain.CustomerProfile;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileRepository {
    boolean existsByUserId(UUID userId);

    Optional<CustomerProfile> findById(UUID id);

    Optional<CustomerProfile> findByUserId(UUID userId);

    CustomerProfile save(CustomerProfile profile);
}
