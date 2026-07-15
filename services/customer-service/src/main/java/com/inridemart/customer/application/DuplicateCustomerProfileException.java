package com.inridemart.customer.application;

public class DuplicateCustomerProfileException extends RuntimeException {
    public DuplicateCustomerProfileException(String message) {
        super(message);
    }
}
