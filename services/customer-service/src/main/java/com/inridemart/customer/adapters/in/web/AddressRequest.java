package com.inridemart.customer.adapters.in.web;

import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 20) String postalCode,
        @Size(max = 100) String country
) {
}
