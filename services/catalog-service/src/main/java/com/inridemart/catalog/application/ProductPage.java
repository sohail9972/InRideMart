package com.inridemart.catalog.application;

import com.inridemart.catalog.domain.Product;

import java.util.List;

public record ProductPage(List<Product> items, int page, int size, long totalItems, int totalPages) {
}
