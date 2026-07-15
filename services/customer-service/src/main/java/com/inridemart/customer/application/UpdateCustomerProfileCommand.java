package com.inridemart.customer.application;

import com.inridemart.customer.domain.Address;

public record UpdateCustomerProfileCommand(String fullName, String phoneNumber, Address address) {
}
