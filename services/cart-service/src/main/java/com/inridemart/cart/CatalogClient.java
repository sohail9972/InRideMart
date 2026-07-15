package com.inridemart.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
class CatalogClient {
    private final RestClient client;

    CatalogClient(@Value("${inridemart.catalog.base-url:http://localhost:8083}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    void requireProduct(UUID productId) {
        client.get().uri("/api/v1/catalog/products/{id}", productId).retrieve().toBodilessEntity();
    }
}
