package com.inridemart.customer.adapters.in.web;

import com.inridemart.customer.domain.Address;

public record AddressResponse(
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country
) {
    static AddressResponse from(Address address) {
        return new AddressResponse(address.addressLine1(), address.addressLine2(), address.city(), address.state(), address.postalCode(), address.country());
    }
}
