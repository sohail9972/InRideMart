package com.inridemart.customer.application;

import com.inridemart.customer.domain.CustomerProfile;
import com.inridemart.customer.domain.ports.CustomerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerProfileQuery {
    private final CustomerProfileRepository profiles;

    public CustomerProfileQuery(CustomerProfileRepository profiles) {
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public CustomerProfile findById(UUID id) {
        return profiles.findById(id)
                .orElseThrow(() -> new CustomerProfileNotFoundException("Customer profile not found"));
    }

    @Transactional(readOnly = true)
    public CustomerProfile findByUserId(UUID userId) {
        return profiles.findByUserId(userId)
                .orElseThrow(() -> new CustomerProfileNotFoundException("Customer profile not found"));
    }
}
