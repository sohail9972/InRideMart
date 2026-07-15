package com.inridemart.customer.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class CustomerProfile {
    private final UUID id;
    private final UUID userId;
    private final String fullName;
    private final String phoneNumber;
    private final Address address;
    private final Instant createdAt;
    private final Instant updatedAt;

    private CustomerProfile(UUID id, UUID userId, String fullName, String phoneNumber, Address address, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.fullName = requireFullName(fullName);
        this.phoneNumber = normalize(phoneNumber);
        this.address = address == null ? new Address(null, null, null, null, null, null) : address;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static CustomerProfile create(UUID userId, String fullName, String phoneNumber, Address address, Instant now) {
        return new CustomerProfile(UUID.randomUUID(), userId, fullName, phoneNumber, address, now, now);
    }

    public static CustomerProfile rehydrate(UUID id, UUID userId, String fullName, String phoneNumber, Address address, Instant createdAt, Instant updatedAt) {
        return new CustomerProfile(id, userId, fullName, phoneNumber, address, createdAt, updatedAt);
    }

    public CustomerProfile update(String fullName, String phoneNumber, Address address, Instant now) {
        return new CustomerProfile(id, userId, fullName, phoneNumber, address, createdAt, now);
    }

    private static String requireFullName(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.length() < 2) {
            throw new IllegalArgumentException("Full name must contain at least 2 characters");
        }
        return normalized;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String fullName() {
        return fullName;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public Address address() {
        return address;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
