package com.inridemart.catalog.adapters.in.web;

import com.inridemart.catalog.domain.Category;

import java.util.UUID;

public record CategoryResponse(UUID id, String slug, String name, String description) {
    static CategoryResponse from(Category category) {
        return new CategoryResponse(category.id(), category.slug(), category.name(), category.description());
    }
}
