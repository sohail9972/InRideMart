package com.inridemart.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
class OpenAiResponsesClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAiResponsesClient.class);
    private final RestClient client = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    private final ObjectMapper mapper;
    private final OpenAiProperties properties;

    OpenAiResponsesClient(ObjectMapper mapper, OpenAiProperties properties) {
        this.mapper = mapper;
        this.properties = properties;
    }

    Optional<IntentDecision> understand(String conversation, String customer) {
        if (!properties.enabled()) return Optional.empty();
        try {
            Map<String, Object> schema = Map.of(
                    "type", "object",
                    "additionalProperties", false,
                    "properties", Map.of(
                            "readyForRecommendations", Map.of("type", "boolean"),
                            "assistantMessage", Map.of("type", "string"),
                            "recommendationReason", Map.of("type", "string"),
                            "intent", Map.of("type", "string"),
                            "budget", Map.of("type", "number"),
                            "searchTerms", Map.of("type", "array", "items", Map.of("type", "string"), "maxItems", 8),
                            "bundleName", Map.of("type", "string")),
                    "required", List.of("readyForRecommendations", "assistantMessage", "recommendationReason", "intent", "budget", "searchTerms", "bundleName"));
            Map<String, Object> body = new HashMap<>(Map.of(
                    "model", properties.model(),
                    "store", false,
                    "max_output_tokens", properties.maxOutputTokens(),
                    "instructions", "You are a concise, friendly InRideMart shopping assistant. First understand the conversation, treating the latest passenger message as authoritative when it changes the shopping topic. Greet greetings naturally. If the shopper has not expressed a clear shopping need, set readyForRecommendations to false and ask one useful clarifying question. Do not guess and do not recommend products at that stage. When the intent is clear, set readyForRecommendations to true, extract budget and context, and provide precise searchTerms for the Catalog Service. searchTerms must describe product concepts, never invented product IDs or unrelated default products. Combine only relevant conversation context. Keep assistantMessage natural and concise. Use budget 0 when no budget was stated.",
                    "input", "Conversation:\n" + conversation + "\nCustomer profile:\n" + customer,
                    "text", Map.of("format", Map.of("type", "json_schema", "name", "shopping_intent", "strict", true, "schema", schema))));
            body.putAll(generationControls());
            JsonNode response = client.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            String json = response.path("output").findValuesAsText("text").stream().findFirst().orElseThrow();
            JsonNode decision = mapper.readTree(json);
            BigDecimal budget = decision.path("budget").asDouble(0) == 0 ? null : decision.path("budget").decimalValue();
            List<String> terms = mapper.convertValue(decision.path("searchTerms"), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            return Optional.of(new IntentDecision(
                    decision.path("readyForRecommendations").asBoolean(),
                    decision.path("assistantMessage").asText(),
                    decision.path("recommendationReason").asText(),
                    decision.path("intent").asText("clarification"),
                    budget,
                    terms,
                    decision.path("bundleName").asText()));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    TranslationAttempt translate(TranslationPrompt prompt) {
        if (!properties.enabled()) return unavailable("OPENAI_API_KEY is missing or blank");
        try {
            Map<String, Object> schema = Map.of(
                    "type", "object",
                    "additionalProperties", false,
                    "properties", Map.of(
                            "passengerLanguage", Map.of("type", "string"),
                            "driverLanguage", Map.of("type", "string"),
                            "driverTranslation", Map.of("type", "string"),
                            "passengerTranslation", Map.of("type", "string"),
                            "supported", Map.of("type", "boolean")),
                    "required", List.of("passengerLanguage", "driverLanguage", "driverTranslation", "passengerTranslation", "supported"));
            String driverReply = prompt.driverReply().isBlank() ? "(no driver reply yet)" : prompt.driverReply();
            Map<String, Object> body = new HashMap<>(Map.of(
                    "model", properties.model(),
                    "store", false,
                    "max_output_tokens", properties.maxOutputTokens(),
                    "instructions", "You are InRideMart's multilingual ride communication assistant. Automatically detect the passenger language. Faithfully translate the passenger message into the requested driver language, preserving meaning and tone. When a driver reply is present, translate it into the detected passenger language. Cover ride products, payment, stops, AC or window requests, luggage, emergency communication, and destination confirmation. Supported languages are English, Hindi, Kannada, Telugu, Tamil, Malayalam, Marathi, Bengali, Gujarati, Punjabi, Urdu, Spanish, French, German, Portuguese, Italian, Dutch, Russian, Arabic, Turkish, Chinese, Japanese, Korean, Thai, Vietnamese, and Indonesian. Do not add advice, products, or facts. Set supported false if the passenger language is unsupported; otherwise return only the requested translations.",
                    "input", "Passenger message: " + prompt.passengerMessage() + "\nDriver language: " + prompt.driverLanguage().displayName() + "\nDriver reply: " + driverReply,
                    "text", Map.of("format", Map.of("type", "json_schema", "name", "ride_translation", "strict", true, "schema", schema))));
            body.putAll(generationControls());
            JsonNode response = client.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            String json = response.path("output").findValuesAsText("text").stream().findFirst().orElseThrow();
            JsonNode decision = mapper.readTree(json);
            TranslationDecision translation = new TranslationDecision(
                    TravelLanguage.fromLabel(decision.path("passengerLanguage").asText()),
                    TravelLanguage.fromLabel(decision.path("driverLanguage").asText()),
                    decision.path("driverTranslation").asText(),
                    decision.path("passengerTranslation").asText(),
                    decision.path("supported").asBoolean());
            log.info("OpenAI translation succeeded using model {}.", properties.model());
            return TranslationAttempt.success(translation);
        } catch (RestClientResponseException exception) {
            String providerReason = providerReason(exception);
            log.warn("OpenAI translation request failed with HTTP {}: {}", exception.getStatusCode().value(), providerReason);
            return unavailable("OpenAI API returned HTTP " + exception.getStatusCode().value() + " (" + providerReason + ")");
        } catch (ResourceAccessException exception) {
            return unavailable("OpenAI request timed out or the network is unavailable");
        } catch (Exception exception) {
            String reason = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            log.warn("OpenAI translation response could not be used: {}", reason);
            return unavailable("OpenAI response could not be parsed (" + reason + ")");
        }
    }

    private Map<String, Object> generationControls() {
        if (properties.model().toLowerCase().startsWith("gpt-5")) {
            return Map.of("reasoning", Map.of("effort", "none"));
        }
        return Map.of("temperature", properties.temperature());
    }

    private String providerReason(RestClientResponseException exception) {
        try {
            JsonNode error = mapper.readTree(exception.getResponseBodyAsString()).path("error");
            String code = error.path("code").asText("");
            String message = error.path("message").asText("");
            String reason = code.isBlank() ? message : code + ": " + message;
            if (!reason.isBlank()) return reason.length() > 240 ? reason.substring(0, 240) : reason;
        } catch (Exception ignored) {
            // Keep provider failures from masking the original request failure.
        }
        return "provider error";
    }

    private TranslationAttempt unavailable(String reason) {
        log.warn("OpenAI translation unavailable: {}. Local fallback will be used.", reason);
        return TranslationAttempt.unavailable(reason);
    }

    record IntentDecision(boolean readyForRecommendations, String assistantMessage, String recommendationReason,
                          String intent, BigDecimal budget, List<String> searchTerms, String bundleName) { }
    record TranslationPrompt(String passengerMessage, TravelLanguage driverLanguage, String driverReply) { }
    record TranslationDecision(TravelLanguage passengerLanguage, TravelLanguage driverLanguage,
                               String driverTranslation, String passengerTranslation, boolean supported) { }
    record TranslationAttempt(TranslationDecision decision, String fallbackReason) {
        static TranslationAttempt success(TranslationDecision decision) {
            return new TranslationAttempt(decision, null);
        }

        static TranslationAttempt unavailable(String fallbackReason) {
            return new TranslationAttempt(null, fallbackReason);
        }

        boolean usedOpenAi() {
            return decision != null;
        }
    }
}
