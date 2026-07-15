package com.inridemart.catalog.adapters.in.web;

import com.inridemart.catalog.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(UUID id, String sku, String name, String description, BigDecimal price, String currency,
                              String imageUrl, CategoryResponse category, Instant createdAt) {
    static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.sku(), product.name(), product.description(), product.price(), product.currency(), product.imageUrl(), CategoryResponse.from(product.category()), product.createdAt());
    }
}
