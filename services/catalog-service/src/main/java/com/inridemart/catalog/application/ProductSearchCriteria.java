package com.inridemart.catalog.application;

import java.math.BigDecimal;

public record ProductSearchCriteria(String query, String category, BigDecimal minPrice, BigDecimal maxPrice,
                                    String sort, int page, int size) {
    public ProductSearchCriteria {
        query = normalize(query);
        category = normalize(category);
        sort = sort == null ? "RELEVANCE" : sort.trim().toUpperCase();
        page = Math.max(0, page);
        size = Math.clamp(size, 1, 50);
        if (minPrice != null && minPrice.signum() < 0) {
            throw new IllegalArgumentException("minPrice cannot be negative");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException("maxPrice cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
