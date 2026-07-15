package com.inridemart.catalog.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {
    @Test
    void rejectsNegativePrice() {
        Category category = new Category(UUID.randomUUID(), "tech", "Tech", "Travel tech");

        assertThatThrownBy(() -> new Product(UUID.randomUUID(), "SKU-1", "Cable", "A cable", new BigDecimal("-1.00"), "INR", null, category, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product price cannot be negative");
    }
}
