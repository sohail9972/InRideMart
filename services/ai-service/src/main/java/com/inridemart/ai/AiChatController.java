package com.inridemart.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
class AiChatController {
    private final AiChatService service;
    private final TravelTranslationService translationService;

    AiChatController(AiChatService service, TravelTranslationService translationService) {
        this.service = service;
        this.translationService = translationService;
    }

    @PostMapping("/chat") AiChatResponse chat(Authentication authentication, @RequestHeader("Authorization") String authorization, @Valid @RequestBody AiChatRequest request) { return service.chat((UUID) authentication.getPrincipal(), authorization, request); }

    @Operation(
            summary = "Translate a passenger-driver ride conversation",
            description = "Automatically detects the passenger language, translates it for the driver, and translates an optional driver reply back for the passenger.")
    @PostMapping("/translate")
    TravelTranslationResponse translate(@Valid @RequestBody TravelTranslationRequest request) {
        return translationService.translate(request);
    }
}
record AiChatRequest(@NotBlank String message, BigDecimal budget, String destination, String weather,
                     Integer journeyDurationMinutes, String travelPurpose, String timeOfDay, List<UUID> availableProductIds,
                     List<ConversationTurn> conversation) {
    AiChatRequest {
        availableProductIds = availableProductIds == null ? List.of() : List.copyOf(availableProductIds);
        conversation = conversation == null ? List.of() : List.copyOf(conversation);
    }
}
record ConversationTurn(String role, String message) { }
record AiChatResponse(String assistantMessage, List<AiProduct> recommendedProducts, String recommendationReason, SuggestedBundle suggestedBundle, boolean readyForRecommendations, String intent) { }
record AiProduct(UUID id, String name, String description, BigDecimal price, String currency, String category, String imageUrl, String reason) {
    static AiProduct from(CatalogClient.CatalogProduct product, String reason) {
        return new AiProduct(product.id(), product.name(), product.description(), product.price(), product.currency(), product.category(), product.imageUrl(), reason);
    }
}
record SuggestedBundle(String name, List<AiProduct> products, BigDecimal totalAmount) { }
record TravelTranslationRequest(@NotBlank String passengerMessage, TravelLanguage driverLanguage, String driverReply) {
    TravelLanguage effectiveDriverLanguage() {
        return driverLanguage == null ? TravelLanguage.KANNADA : driverLanguage;
    }
}
record TravelTranslationResponse(String passengerMessage, TravelLanguage passengerLanguage, TravelLanguage driverLanguage,
                                 String driverTranslation, String driverReply, String passengerTranslation,
                                 boolean supported, boolean fallbackUsed, String fallbackReason) { }
