package com.inridemart.catalog.adapters.in.web;

import com.inridemart.catalog.application.ProductPage;

import java.util.List;

public record ProductPageResponse(List<ProductResponse> items, int page, int size, long totalItems, int totalPages) {
    static ProductPageResponse from(ProductPage page) {
        return new ProductPageResponse(page.items().stream().map(ProductResponse::from).toList(), page.page(), page.size(), page.totalItems(), page.totalPages());
    }
}
