package com.inridemart.customer.application;

import com.inridemart.customer.domain.Address;

import java.util.UUID;

public record CreateCustomerProfileCommand(UUID userId, String fullName, String phoneNumber, Address address) {
}
