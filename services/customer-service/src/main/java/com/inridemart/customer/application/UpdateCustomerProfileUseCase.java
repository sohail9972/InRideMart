package com.inridemart.customer.application;

import com.inridemart.customer.domain.CustomerProfile;
import com.inridemart.customer.domain.ports.CustomerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateCustomerProfileUseCase {
    private final CustomerProfileRepository profiles;
    private final Clock clock;

    public UpdateCustomerProfileUseCase(CustomerProfileRepository profiles, Clock clock) {
        this.profiles = profiles;
        this.clock = clock;
    }

    @Transactional
    public CustomerProfile update(UUID id, UpdateCustomerProfileCommand command) {
        CustomerProfile existing = profiles.findById(id)
                .orElseThrow(() -> new CustomerProfileNotFoundException("Customer profile not found"));

        CustomerProfile updated = existing.update(command.fullName(), command.phoneNumber(), command.address(), Instant.now(clock));
        return profiles.save(updated);
    }
}
