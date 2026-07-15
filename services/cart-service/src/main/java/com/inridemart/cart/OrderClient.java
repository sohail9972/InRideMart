package com.inridemart.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
class OrderClient {
    private final RestClient client;

    OrderClient(@Value("${inridemart.order.base-url:http://localhost:8085}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    Object create(List<CartController.Item> items, String bearerToken, String idempotencyKey) {
        return client.post().uri("/api/v1/orders").header("Authorization", bearerToken).header("Idempotency-Key", idempotencyKey).body(Map.of("items", items)).retrieve().body(Object.class);
    }
}
