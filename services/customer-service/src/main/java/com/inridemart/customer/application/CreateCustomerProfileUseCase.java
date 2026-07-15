package com.inridemart.customer.application;

import com.inridemart.customer.domain.CustomerProfile;
import com.inridemart.customer.domain.ports.CustomerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class CreateCustomerProfileUseCase {
    private final CustomerProfileRepository profiles;
    private final Clock clock;

    public CreateCustomerProfileUseCase(CustomerProfileRepository profiles, Clock clock) {
        this.profiles = profiles;
        this.clock = clock;
    }

    @Transactional
    public CustomerProfile create(CreateCustomerProfileCommand command) {
        if (profiles.existsByUserId(command.userId())) {
            throw new DuplicateCustomerProfileException("Customer profile already exists for user");
        }

        Instant now = Instant.now(clock);
        CustomerProfile profile = CustomerProfile.create(command.userId(), command.fullName(), command.phoneNumber(), command.address(), now);
        return profiles.save(profile);
    }
}
