package com.inridemart.customer.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCustomerProfileRepository extends JpaRepository<CustomerProfileEntity, UUID> {
    boolean existsByUserId(UUID userId);

    Optional<CustomerProfileEntity> findByUserId(UUID userId);
}
