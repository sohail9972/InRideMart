package com.inridemart.customer.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerProfileTest {
    @Test
    void createsProfileWithNormalizedName() {
        CustomerProfile profile = CustomerProfile.create(UUID.randomUUID(), "  Sana Khan  ", null, null, Instant.parse("2026-07-14T00:00:00Z"));

        assertThat(profile.fullName()).isEqualTo("Sana Khan");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> CustomerProfile.create(UUID.randomUUID(), " ", null, null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Full name must contain at least 2 characters");
    }
}
