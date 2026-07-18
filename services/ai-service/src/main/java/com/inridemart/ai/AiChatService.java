package com.inridemart.ai;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class AiChatService {
    private static final Pattern BUDGET_IN_MESSAGE = Pattern.compile("(?:₹|inr|rs\\.?)\\s*(\\d+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE);
    private final CatalogClient catalog; private final CustomerClient customer; private final OpenAiResponsesClient openAi;
    AiChatService(CatalogClient catalog, CustomerClient customer, OpenAiResponsesClient openAi) { this.catalog = catalog; this.customer = customer; this.openAi = openAi; }
    AiChatResponse chat(UUID userId, String authorization, AiChatRequest request) {
        List<CatalogClient.CatalogProduct> products = catalog.activeProducts();
        BigDecimal budget = effectiveBudget(request);
        String context = request.message() + optional(" Destination: ", request.destination()) + optional(" Weather: ", request.weather()) + (budget == null ? "" : " Budget: " + budget + " INR");
        Optional<OpenAiResponsesClient.Decision> decision = openAi.recommend(context, customer.profileSummary(userId, authorization), products);
        List<CatalogClient.CatalogProduct> selected = decision.map(value -> byIds(products, value.productIds())).filter(value -> !value.isEmpty()).orElseGet(() -> fallback(products, request, budget));
        if (selected.isEmpty()) selected = products.stream().filter(product -> budget == null || product.price().compareTo(budget) <= 0).limit(3).toList();
        String reason = decision.map(OpenAiResponsesClient.Decision::recommendationReason).filter(value -> !value.isBlank()).orElseGet(() -> fallbackReason(request, budget));
        String message = decision.map(OpenAiResponsesClient.Decision::assistantMessage).filter(value -> !value.isBlank()).orElseGet(() -> fallbackMessage(request));
        return new AiChatResponse(message, selected.stream().map(AiProduct::from).toList(), reason, selected.size() > 1 ? new SuggestedBundle("Ride-ready bundle", selected.stream().map(AiProduct::from).toList(), selected.stream().map(CatalogClient.CatalogProduct::price).reduce(BigDecimal.ZERO, BigDecimal::add)) : null);
    }
    private List<CatalogClient.CatalogProduct> fallback(List<CatalogClient.CatalogProduct> products, AiChatRequest request, BigDecimal budget) {
        String text = (request.message() + " " + optional("", request.destination()) + " " + optional("", request.weather())).toLowerCase(Locale.ROOT);
        return products.stream().filter(p -> budget == null || p.price().compareTo(budget) <= 0).sorted(Comparator.comparingInt((CatalogClient.CatalogProduct p) -> score(p, text)).reversed().thenComparing(CatalogClient.CatalogProduct::price)).limit(3).toList();
    }
    private int score(CatalogClient.CatalogProduct product, String text) {
        String source = (product.name() + " " + product.description() + " " + product.category()).toLowerCase(Locale.ROOT);
        int score = 0;
        if (containsAny(text, "hungry", "food", "snack", "eat", "thirsty", "drink", "coffee")) score += containsAny(source, "snack", "coffee", "trail mix", "sips") ? 12 : 0;
        if (containsAny(text, "battery", "charger", "charge", "phone", "usb", "power")) score += containsAny(source, "charger", "charging", "usb", "cable") ? 14 : 0;
        if (containsAny(text, "airport", "flight", "plane", "terminal", "travel")) score += containsAny(source, "travel", "journey", "neck pillow", "charger", "cable", "coffee") ? 8 : 0;
        if (containsAny(text, "rain", "raining", "rainy", "monsoon", "wet")) score += containsAny(source, "umbrella", "rain") ? 16 : 0;
        if (containsAny(text, "hot", "heat", "sunny", "beach")) score += containsAny(source, "cooling", "face mist", "lip balm") ? 8 : 0;
        return score;
    }
    private boolean containsAny(String text, String... values) { return java.util.Arrays.stream(values).anyMatch(text::contains); }
    private BigDecimal effectiveBudget(AiChatRequest request) {
        if (request.budget() != null) return request.budget();
        Matcher match = BUDGET_IN_MESSAGE.matcher(request.message());
        return match.find() ? new BigDecimal(match.group(1)) : null;
    }
    private String fallbackMessage(AiChatRequest request) {
        String text = request.message().toLowerCase(Locale.ROOT);
        if (containsAny(text, "hungry", "food", "snack", "eat")) return "I found a quick snack and drink option for your ride.";
        if (containsAny(text, "battery", "charger", "charge", "phone", "usb")) return "I found charging essentials from the live catalog.";
        if (containsAny(text, "rain", "raining", "rainy", "monsoon")) return "I found weather-ready essentials for the rain.";
        if (containsAny(text, "airport", "flight", "plane", "terminal")) return "I found practical airport-ride essentials.";
        return "Here are useful picks from the live InRideMart catalog.";
    }
    private String fallbackReason(AiChatRequest request, BigDecimal budget) {
        String reason = "Ranked from the live catalog for your request";
        if (request.destination() != null && !request.destination().isBlank()) reason += " and destination";
        if (request.weather() != null && !request.weather().isBlank()) reason += " and weather";
        return reason + (budget == null ? "." : "; every recommendation is within your INR " + budget.stripTrailingZeros().toPlainString() + " budget.");
    }
    private List<CatalogClient.CatalogProduct> byIds(List<CatalogClient.CatalogProduct> products, List<UUID> ids) { return ids.stream().map(id -> products.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null)).filter(java.util.Objects::nonNull).toList(); }
    private String optional(String prefix, String value) { return value == null || value.isBlank() ? "" : prefix + value; }
}
