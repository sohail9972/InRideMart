package com.inridemart.catalog.domain;

import java.util.UUID;

public record Category(UUID id, String slug, String name, String description) {
}
