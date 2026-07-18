package com.inridemart.ai;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AiChatServiceTest {
    @Test void fallsBackToCatalogProductsWithinBudget() {
        CatalogClient catalog = mock(CatalogClient.class); CustomerClient customer = mock(CustomerClient.class); OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        UUID charger = UUID.randomUUID(); when(catalog.activeProducts()).thenReturn(List.of(new CatalogClient.CatalogProduct(charger, "Car Charger", "Fast charger", new BigDecimal("799"), "INR", "Travel tech", null), new CatalogClient.CatalogProduct(UUID.randomUUID(), "Neck Pillow", "Comfort", new BigDecimal("899"), "INR", "Journey", null)));
        when(openAi.recommend(anyString(), anyString(), anyList())).thenReturn(java.util.Optional.empty()); when(customer.profileSummary(any(), anyString())).thenReturn("");
        AiChatResponse response = new AiChatService(catalog, customer, openAi).chat(UUID.randomUUID(), "Bearer token", new AiChatRequest("I forgot my charger", new BigDecimal("800"), null, null));
        assertEquals(1, response.recommendedProducts().size()); assertEquals(charger, response.recommendedProducts().getFirst().id());
    }
}
