package com.inridemart.customer.application;

public class CustomerProfileNotFoundException extends RuntimeException {
    public CustomerProfileNotFoundException(String message) {
        super(message);
    }
}
