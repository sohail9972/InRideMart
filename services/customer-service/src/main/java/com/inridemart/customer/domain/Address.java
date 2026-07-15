package com.inridemart.customer.domain;

public record Address(
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country
) {
}
