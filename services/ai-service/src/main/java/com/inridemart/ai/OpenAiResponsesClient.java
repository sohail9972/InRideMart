package com.inridemart.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Component
class OpenAiResponsesClient {
    private final RestClient client = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    private final ObjectMapper mapper;
    private final OpenAiProperties properties;
    OpenAiResponsesClient(ObjectMapper mapper, OpenAiProperties properties) { this.mapper = mapper; this.properties = properties; }

    Optional<Decision> recommend(String request, String customer, List<CatalogClient.CatalogProduct> products) {
        if (!properties.enabled()) return Optional.empty();
        try {
            String catalog = products.stream().map(p -> p.id() + "|" + p.name() + "|" + p.price() + " " + p.currency() + "|" + p.category()).reduce((a, b) -> a + "\\n" + b).orElse("");
            Map<String, Object> schema = Map.of("type", "object", "additionalProperties", false, "properties", Map.of(
                    "assistantMessage", Map.of("type", "string"), "recommendationReason", Map.of("type", "string"),
                    "productIds", Map.of("type", "array", "items", Map.of("type", "string"), "maxItems", 4),
                    "bundleName", Map.of("type", "string")), "required", List.of("assistantMessage", "recommendationReason", "productIds", "bundleName"));
            Map<String, Object> body = Map.of("model", properties.model(), "store", false, "temperature", properties.temperature(), "max_output_tokens", properties.maxOutputTokens(),
                    "instructions", "You are an in-ride shopping assistant. Infer the shopper's primary intent from needs, destination, weather, and budget. Return only productIds supplied in the catalog. Never invent products, prices, stock, or IDs. Apply an explicit budget as a hard limit per recommended product. Prefer a focused set of up to four products and explain the specific request signals that caused each recommendation.",
                    "input", "Request: " + request + "\\nCustomer: " + customer + "\\nCatalog:\n" + catalog,
                    "text", Map.of("format", Map.of("type", "json_schema", "name", "shopping_recommendation", "strict", true, "schema", schema)));
            JsonNode response = client.post().uri("/responses").contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + properties.apiKey()).body(body).retrieve().body(JsonNode.class);
            String text = response.path("output").findValuesAsText("text").stream().findFirst().orElseThrow();
            JsonNode decision = mapper.readTree(text);
            return Optional.of(new Decision(decision.path("assistantMessage").asText(), decision.path("recommendationReason").asText(), StreamSupport.stream(decision.path("productIds").spliterator(), false).map(JsonNode::asText).map(value -> { try { return UUID.fromString(value); } catch (IllegalArgumentException ignored) { return null; } }).filter(java.util.Objects::nonNull).toList(), decision.path("bundleName").asText()));
        } catch (Exception ignored) { return Optional.empty(); }
    }
    record Decision(String assistantMessage, String recommendationReason, List<UUID> productIds, String bundleName) { }
}
