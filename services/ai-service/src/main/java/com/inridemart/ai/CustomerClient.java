package com.inridemart.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;

@Component
class CustomerClient {
    private final RestClient client;
    CustomerClient(@Value("${inridemart.customer.base-url}") String baseUrl) { client = RestClient.builder().baseUrl(baseUrl).build(); }
    String profileSummary(UUID userId, String authorization) {
        try {
            JsonNode profile = client.get().uri("/api/v1/customers/by-user/{id}", userId).header("Authorization", authorization).retrieve().body(JsonNode.class);
            return profile.path("fullName").asText("") + " in " + profile.path("address").path("city").asText("");
        } catch (RuntimeException ignored) { return ""; }
    }
}
