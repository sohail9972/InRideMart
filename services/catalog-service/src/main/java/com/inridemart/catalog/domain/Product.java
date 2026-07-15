package com.inridemart.catalog.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Product(UUID id, String sku, String name, String description, BigDecimal price, String currency,
                      String imageUrl, Category category, Instant createdAt) {
    public Product {
        Objects.requireNonNull(id);
        Objects.requireNonNull(sku);
        Objects.requireNonNull(name);
        Objects.requireNonNull(price);
        Objects.requireNonNull(currency);
        Objects.requireNonNull(category);
        Objects.requireNonNull(createdAt);
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
    }
}
