package com.inridemart.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
class CatalogClient {
    private final RestClient client;
    CatalogClient(@Value("${inridemart.catalog.base-url}") String baseUrl) { client = RestClient.builder().baseUrl(baseUrl).build(); }
    List<CatalogProduct> activeProducts() {
        JsonNode items = client.get().uri("/api/v1/catalog/products?size=50").retrieve().body(JsonNode.class).path("items");
        List<CatalogProduct> products = new ArrayList<>();
        items.forEach(node -> products.add(new CatalogProduct(UUID.fromString(node.path("id").asText()), node.path("name").asText(), node.path("description").asText(), node.path("price").decimalValue(), node.path("currency").asText(), node.path("category").path("name").asText(), node.path("imageUrl").isNull() ? null : node.path("imageUrl").asText())));
        return products;
    }
    record CatalogProduct(UUID id, String name, String description, BigDecimal price, String currency, String category, String imageUrl) { }
}
