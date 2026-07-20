package com.inridemart.ai;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class AiChatService {
    private static final Pattern BUDGET_IN_MESSAGE = Pattern.compile("(?:\\u20B9|inr|rs\\.?)\\s*(\\d+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE);

    private final CatalogClient catalog;
    private final CustomerClient customer;
    private final OpenAiResponsesClient openAi;

    AiChatService(CatalogClient catalog, CustomerClient customer, OpenAiResponsesClient openAi) {
        this.catalog = catalog;
        this.customer = customer;
        this.openAi = openAi;
    }

    AiChatResponse chat(UUID userId, String authorization, AiChatRequest request) {
        OpenAiResponsesClient.IntentDecision decision = fallbackIntent(request);
        if (!decision.readyForRecommendations() && "clarification".equals(decision.intent())) {
            String customerSummary = customer.profileSummary(userId, authorization);
            OpenAiResponsesClient.IntentDecision localDecision = decision;
            decision = openAi.understand(conversationText(request), customerSummary)
                    .filter(modelDecision -> !modelDecision.readyForRecommendations())
                    .orElse(localDecision);
        }

        if (!decision.readyForRecommendations()) {
            return new AiChatResponse(decision.assistantMessage(), List.of(), decision.recommendationReason(), null, false, decision.intent());
        }

        String intent = decision.intent();
        BigDecimal budget = request.budget() != null ? request.budget() : decision.budget();
        List<CatalogClient.CatalogProduct> products = catalog.activeProducts().stream()
                .filter(product -> request.availableProductIds().isEmpty() || request.availableProductIds().contains(product.id()))
                .toList();
        List<CatalogClient.CatalogProduct> selected = select(products, decision.searchTerms(), budget, intent);
        if (selected.isEmpty()) {
            return new AiChatResponse(
                    unavailableMessage(intent, budget),
                    List.of(),
                    "The live catalog did not contain a matching item within the requested constraints.",
                    null,
                    true,
                    intent);
        }

        List<AiProduct> recommendations = selected.stream()
                .map(product -> AiProduct.from(product, productReason(product, intent, budget)))
                .toList();
        String bundleName = blank(decision.bundleName()) ? defaultBundleName(intent) : decision.bundleName();
        BigDecimal total = selected.stream().map(CatalogClient.CatalogProduct::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        SuggestedBundle bundle = selected.size() > 1 ? new SuggestedBundle(bundleName, recommendations, total) : null;
        return new AiChatResponse(decision.assistantMessage(), recommendations, decision.recommendationReason(), bundle, true, intent);
    }

    private List<CatalogClient.CatalogProduct> select(List<CatalogClient.CatalogProduct> products, List<String> terms, BigDecimal budget, String intent) {
        List<String> normalizedTerms = terms == null ? List.of() : terms.stream().filter(term -> !blank(term)).map(term -> term.toLowerCase(Locale.ROOT)).toList();
        List<CatalogClient.CatalogProduct> ranked = products.stream()
                .filter(product -> budget == null || product.price().compareTo(budget) <= 0)
                .map(product -> new ScoredProduct(product, score(product, normalizedTerms, intent)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(ScoredProduct::score).reversed().thenComparing(scored -> scored.product().price()))
                .map(ScoredProduct::product)
                .toList();

        List<CatalogClient.CatalogProduct> selected = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        if ("airport-travel".equals(intent)) {
            for (String term : normalizedTerms) {
                Optional<CatalogClient.CatalogProduct> match = ranked.stream()
                        .filter(product -> !selected.contains(product))
                        .filter(product -> score(product, List.of(term), "") > 0)
                        .findFirst();
                if (match.isEmpty() || (budget != null && total.add(match.get().price()).compareTo(budget) > 0)) continue;
                selected.add(match.get());
                total = total.add(match.get().price());
                if (selected.size() == 3) return selected;
            }
        }
        for (CatalogClient.CatalogProduct product : ranked) {
            if (selected.size() == 3) break;
            if (selected.contains(product)) continue;
            if (budget != null && total.add(product.price()).compareTo(budget) > 0) continue;
            selected.add(product);
            total = total.add(product.price());
        }
        return selected;
    }

    private int score(CatalogClient.CatalogProduct product, List<String> terms, String intent) {
        String source = (product.name() + " " + product.description() + " " + product.category()).toLowerCase(Locale.ROOT);
        int score = terms.stream().mapToInt(term -> source.contains(term) ? 10 : partialMatch(source, term) ? 4 : 0).sum();
        if (intent.contains("airport") || intent.equals("travel")) score += containsAny(source, "charger", "cable", "pillow", "coffee") ? 12 : 0;
        if (intent.contains("rain")) score += containsAny(source, "umbrella", "rain", "poncho") ? 12 : 0;
        if (intent.contains("snack") && terms.size() == 1 && "snack".equals(terms.getFirst())) score += containsAny(source, "trail mix", "coffee", "snack", "sips", "chips", "protein", "chocolate", "cookie", "soda", "energy", "water", "juice", "tea", "mint", "dry fruit", "noodle", "sandwich") ? 12 : 0;
        if (intent.contains("charg")) score += containsAny(source, "charger", "cable", "usb", "power bank", "earphone") ? 12 : 0;
        if (intent.contains("comfort")) score += containsAny(source, "pillow", "eye mask") ? 12 : 0;
        if (intent.contains("hygiene")) score += containsAny(source, "wipes", "tissue", "sanitizer") ? 12 : 0;
        if (intent.contains("wellness")) score += containsAny(source, "pain", "motion") ? 12 : 0;
        return score;
    }

    private boolean partialMatch(String source, String term) {
        return term.contains("charger") && source.contains("charge")
                || term.contains("snack") && source.contains("sips")
                || term.contains("headphone") && source.contains("earphone")
                || term.contains("earphone") && source.contains("headphone")
                || term.contains("rain") && source.contains("umbrella")
                || term.contains("airport") && source.contains("journey")
                || term.contains("travel") && source.contains("journey");
    }

    private String conversationText(AiChatRequest request) {
        StringBuilder text = new StringBuilder();
        for (ConversationTurn turn : request.conversation()) {
            if (turn != null && !blank(turn.message())) text.append(turn.role()).append(": ").append(turn.message()).append("\n");
        }
        text.append("user: ").append(request.message());
        if (!blank(request.destination())) text.append("\nDestination context: ").append(request.destination());
        if (!blank(request.weather())) text.append("\nWeather context: ").append(request.weather());
        if (request.budget() != null) text.append("\nBudget context: INR ").append(request.budget());
        if (request.journeyDurationMinutes() != null) text.append("\nJourney duration: ").append(request.journeyDurationMinutes()).append(" minutes");
        if (!blank(request.travelPurpose())) text.append("\nTravel purpose: ").append(request.travelPurpose());
        if (!blank(request.timeOfDay())) text.append("\nTime of day: ").append(request.timeOfDay());
        return text.toString();
    }

    private OpenAiResponsesClient.IntentDecision fallbackIntent(AiChatRequest request) {
        String currentMessage = request.message().trim().toLowerCase(Locale.ROOT);
        if (currentMessage.matches("^(hi|hello|hey)[!.?]*$")) {
            return clarify("Hi! Welcome to InRideMart. I am your shopping assistant. Where are you heading or what do you need today?", "greeting", "Tell me what you need");
        }

        String shopperText = shoppingTextForCurrentTurn(request, currentMessage);
        String rideText = (optional("", request.destination()) + " " + optional("", request.weather()) + " " + optional("", request.travelPurpose())).toLowerCase(Locale.ROOT);
        BigDecimal budget = request.budget() != null ? request.budget() : budgetFrom(shopperText);
        boolean charger = containsAny(shopperText, "charger", "charging", "battery", "usb", "power bank", "cable");
        boolean audio = containsAny(shopperText, "earphone", "earphones", "headphone", "headphones", "headset");
        boolean hunger = containsAny(shopperText, "hungry", "food", "snack", "eat", "thirsty", "drink", "coffee", "chips", "protein", "chocolate", "cookie", "biscuit", "soda", "energy drink", "water", "juice", "tea", "gum", "mint", "dry fruit", "noodle", "sandwich");
        boolean rain = containsAny(shopperText, "rain", "raining", "rainy", "monsoon", "wet", "poncho", "umbrella");
        boolean comfort = containsAny(shopperText, "neck pillow", "eye mask", "rest", "nap", "sleep");
        boolean hygiene = containsAny(shopperText, "wipes", "tissue", "sanitizer", "clean");
        boolean medicine = containsAny(shopperText, "medicine", "medication", "tablet", "tablets", "pain", "sore", "motion sickness", "nausea");
        boolean faceMask = containsAny(shopperText, "face mask", "sheet mask", "skincare mask");
        boolean airport = containsAny(shopperText, "airport", "flight", "plane", "terminal");
        boolean travel = containsAny(shopperText, "travel", "travelling", "trip", "journey", "road trip");
        boolean kids = containsAny(shopperText, "kids", "children", "child");
        boolean bundleRequest = containsAny(shopperText, "essential", "essentials", "bundle", "kit", "pack");
        boolean vagueSnackRequest = hasOnlyGenericSnackIntent(currentMessage);
        boolean directNeed = charger || audio || hunger || rain || comfort || hygiene || medicine || faceMask;

        if (!directNeed && !airport && !travel && !kids && budget == null) {
            return clarify("I can help with that. Are you looking for travel essentials, snacks, charging, or something for the weather?", "clarification", "Clarify the shopping need");
        }
        if (budget != null && !directNeed && !airport && !travel && !kids) {
            return clarify("What would you like to find within your budget? For example, snacks, a charger, or travel essentials.", "clarification", "Clarify the product need");
        }
        if (vagueSnackRequest) {
            return clarify("What type of snack are you looking for: something savoury, sweet, high-protein, or a drink?", "clarification", "Clarify the snack preference");
        }
        if ((airport || travel || kids) && !directNeed && !bundleRequest && budget == null) {
            String subject = airport ? "airport trip" : kids ? "traveling with kids" : "journey";
            return clarify("That sounds like a " + subject + ". Do you have a budget, and is there anything specific you need for the trip?", "clarification", "Collect trip constraints");
        }

        List<String> terms = new ArrayList<>();
        if (charger) addTermsForMentionedKeywords(terms, shopperText, "charger", "cable", "usb", "power bank");
        if (charger && terms.isEmpty()) terms.addAll(List.of("charger", "cable", "usb"));
        if (audio) terms.add("earphone");
        int foodTermCount = terms.size();
        if (hunger) addTermsForMentionedKeywords(terms, shopperText, "snack", "coffee", "chips", "protein", "chocolate", "cookie", "biscuit", "soda", "energy", "water", "juice", "tea", "gum", "mint", "dry fruit", "noodle", "sandwich");
        if (hunger && terms.size() == foodTermCount) terms.addAll(List.of("snack", "coffee", "sips", "chips", "protein", "chocolate", "cookie", "soda", "energy", "water", "juice", "tea", "mint", "dry fruit", "noodle", "sandwich"));
        if (rain || (!directNeed && containsAny(rideText, "rain", "rainy", "monsoon"))) terms.addAll(List.of("umbrella", "poncho"));
        if (comfort) terms.addAll(List.of("pillow", "eye mask"));
        if (hygiene) terms.addAll(List.of("wipes", "tissue", "sanitizer"));
        if (medicine) terms.addAll(List.of("motion", "pain"));
        if (faceMask) terms.add("eye mask");
        if ((airport || travel) && (!directNeed || bundleRequest)) terms.addAll(List.of("neck pillow", "charger", "cable", "coffee"));
        if (kids) terms.addAll(List.of("snack", "travel"));
        String intent = faceMask ? "face-mask-unavailable" : medicine ? "medicine-request" : charger ? "charging" : audio ? "audio" : hunger ? "snacks" : rain ? "rainy-day" : comfort ? "comfort" : hygiene ? "hygiene" : airport ? "airport-travel" : kids ? "family-travel" : "travel";
        String message = charger ? "I understand you need charging essentials. Let me find suitable options from the live catalog."
                : audio ? "I understand you are looking for headphones. Let me check the available in-cab audio options."
                : hunger ? "I understand you are looking for something to eat or drink. Let me find a quick ride-friendly option."
                : rain ? "I understand you need something for rainy weather. Let me check the live catalog."
                : faceMask ? "I do not see a face mask in the current cab inventory. The closest available comfort item is an eye mask."
                : comfort ? "I understand you would like to be more comfortable during the ride. Let me check the live catalog."
                : hygiene ? "I understand you need a quick hygiene essential. Let me check the live catalog."
                : medicine ? "I do not carry real medicine. I can only show the catalog's demo-only motion-comfort item and topical relief option; please seek medical advice for health concerns."
                : "I understand the trip context. Let me find useful essentials within your constraints.";
        return new OpenAiResponsesClient.IntentDecision(true, message, "I matched your current request against the live catalog" + (budget == null ? "." : " and kept the recommendations within your INR " + budget.stripTrailingZeros().toPlainString() + " budget."), intent, budget, terms, defaultBundleName(intent));
    }

    private OpenAiResponsesClient.IntentDecision clarify(String message, String intent, String reason) {
        return new OpenAiResponsesClient.IntentDecision(false, message, reason + ".", intent, null, List.of(), "");
    }

    private BigDecimal budgetFrom(String text) {
        Matcher matcher = BUDGET_IN_MESSAGE.matcher(text);
        return matcher.find() ? new BigDecimal(matcher.group(1)) : null;
    }

    private String productReason(CatalogClient.CatalogProduct product, String intent, BigDecimal budget) {
        String source = (product.name() + " " + product.description()).toLowerCase(Locale.ROOT);
        String reason = "face-mask-unavailable".equals(intent) ? "Face masks are not in the catalog; this is the closest available comfort alternative."
                : "medicine-request".equals(intent) ? "This is a catalog item for travel comfort only, not medical advice or a substitute for real medicine."
                : "audio".equals(intent) ? "It is the available in-cab audio option for calls, music, or movies."
                : source.contains("charg") || source.contains("usb") || source.contains("cable") || source.contains("power bank") ? "It helps keep your phone or device powered during the ride."
                : source.contains("umbrella") || source.contains("poncho") ? "It is compact protection for the rainy-weather context you shared."
                : source.contains("snack") || source.contains("coffee") || source.contains("cranber") || source.contains("sips") || source.contains("chips") || source.contains("protein") || source.contains("chocolate") || source.contains("cookie") || source.contains("soda") || source.contains("water") || source.contains("juice") || source.contains("tea") || source.contains("noodle") || source.contains("sandwich") ? "It is an easy snack or drink for the journey."
                : source.contains("pillow") || source.contains("eye mask") || source.contains("earphone") ? "It adds comfort for a longer journey or airport transfer."
                : source.contains("wipe") || source.contains("tissue") || source.contains("sanitizer") ? "It is a useful hygiene essential to keep within reach during the ride."
                : source.contains("pain") || source.contains("motion") ? "It matches the travel-comfort need you shared."
                : "It matches the travel context you shared.";
        return budget == null ? reason : reason + " It also fits your INR " + budget.stripTrailingZeros().toPlainString() + " budget.";
    }

    private String defaultBundleName(String intent) {
        return switch (intent) {
            case "airport-travel" -> "Airport Travel Kit";
            case "charging" -> "Charging Essentials";
            case "audio" -> "Audio Essentials";
            case "snacks" -> "Ride Snack Pack";
            case "rainy-day" -> "Rainy Day Kit";
            case "comfort" -> "Comfort Kit";
            case "hygiene" -> "Freshen Up Kit";
            case "medicine-request" -> "Travel Comfort Options";
            case "family-travel" -> "Family Travel Pack";
            default -> "Travel Essentials";
        };
    }

    private String unavailableMessage(String intent, BigDecimal budget) {
        String constraint = budget == null ? "the current cab inventory" : "your INR " + budget.stripTrailingZeros().toPlainString() + " budget";
        return switch (intent) {
            case "face-mask-unavailable" -> "Face masks are not available in this cab, and the closest comfort alternative is unavailable under the current constraints.";
            case "medicine-request" -> "I do not have a suitable catalog item for that health request. Please seek appropriate medical assistance rather than relying on in-ride products.";
            default -> "I could not find a matching product in " + constraint + ".";
        };
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }

    private void addTermsForMentionedKeywords(List<String> terms, String text, String... values) {
        for (String value : values) if (text.contains(value)) terms.add(value);
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    private String shoppingTextForCurrentTurn(AiChatRequest request, String currentMessage) {
        if (!isBudgetOnlyFollowUp(currentMessage)) return currentMessage;
        for (int index = request.conversation().size() - 1; index >= 0; index--) {
            ConversationTurn turn = request.conversation().get(index);
            if (turn != null && "user".equalsIgnoreCase(turn.role()) && !blank(turn.message())) {
                return turn.message().toLowerCase(Locale.ROOT) + "\n" + currentMessage;
            }
        }
        return currentMessage;
    }

    private boolean isBudgetOnlyFollowUp(String message) {
        if (budgetFrom(message) == null) return false;
        String remaining = BUDGET_IN_MESSAGE.matcher(message).replaceAll("")
                .replaceAll("\\b(i|have|my|budget|is|of|around|about|within|for|under|rupees|inr|rs)\\b", "")
                .replaceAll("[^a-z]", "");
        return remaining.isBlank();
    }

    private boolean hasOnlyGenericSnackIntent(String message) {
        if (!containsAny(message, "snack")) return false;
        return !containsAny(message, "chips", "protein", "chocolate", "cookie", "biscuit", "coffee", "water", "juice", "soda", "energy", "tea", "mint", "gum", "dry fruit", "noodle", "sandwich", "hungry");
    }

    private String optional(String prefix, String value) { return blank(value) ? "" : prefix + value; }

    private record ScoredProduct(CatalogClient.CatalogProduct product, int score) { }
}
