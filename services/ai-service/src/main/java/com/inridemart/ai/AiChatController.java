package com.inridemart.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    AiChatController(AiChatService service) { this.service = service; }
    @PostMapping("/chat") AiChatResponse chat(Authentication authentication, @RequestHeader("Authorization") String authorization, @Valid @RequestBody AiChatRequest request) { return service.chat((UUID) authentication.getPrincipal(), authorization, request); }
}
record AiChatRequest(@NotBlank String message, BigDecimal budget, String destination, String weather) { }
record AiChatResponse(String assistantMessage, List<AiProduct> recommendedProducts, String recommendationReason, SuggestedBundle suggestedBundle) { }
record AiProduct(UUID id, String name, String description, BigDecimal price, String currency, String category, String imageUrl) { static AiProduct from(CatalogClient.CatalogProduct p) { return new AiProduct(p.id(), p.name(), p.description(), p.price(), p.currency(), p.category(), p.imageUrl()); } }
record SuggestedBundle(String name, List<AiProduct> products, BigDecimal totalAmount) { }
