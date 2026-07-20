package com.inridemart.ai;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AiChatServiceTest {
    @Test void fallsBackToCatalogProductsWithinBudget() {
        CatalogClient catalog = mock(CatalogClient.class); CustomerClient customer = mock(CustomerClient.class); OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        UUID charger = UUID.randomUUID(); when(catalog.activeProducts()).thenReturn(List.of(new CatalogClient.CatalogProduct(charger, "Car Charger", "Fast charger", new BigDecimal("799"), "INR", "Travel tech", null), new CatalogClient.CatalogProduct(UUID.randomUUID(), "Neck Pillow", "Comfort", new BigDecimal("899"), "INR", "Journey", null)));
        when(openAi.understand(anyString(), anyString())).thenReturn(java.util.Optional.empty()); when(customer.profileSummary(any(), anyString())).thenReturn("");
        AiChatResponse response = new AiChatService(catalog, customer, openAi).chat(UUID.randomUUID(), "Bearer token", new AiChatRequest("I forgot my charger", new BigDecimal("800"), null, null, null, null, null, List.of(), List.of()));
        assertEquals(1, response.recommendedProducts().size()); assertEquals(charger, response.recommendedProducts().getFirst().id());
    }

    @Test void greetsWithoutQueryingCatalog() {
        CatalogClient catalog = mock(CatalogClient.class); CustomerClient customer = mock(CustomerClient.class); OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        when(openAi.understand(anyString(), anyString())).thenReturn(java.util.Optional.empty()); when(customer.profileSummary(any(), anyString())).thenReturn("");

        AiChatResponse response = new AiChatService(catalog, customer, openAi).chat(UUID.randomUUID(), "Bearer token", new AiChatRequest("Hi", null, "Airport", "Rainy", 35, "Business", "morning", List.of(), List.of()));

        assertFalse(response.readyForRecommendations()); assertEquals(0, response.recommendedProducts().size()); verifyNoInteractions(catalog);
    }

    @Test void carriesAirportContextIntoFollowUpBudget() {
        CatalogClient catalog = mock(CatalogClient.class); CustomerClient customer = mock(CustomerClient.class); OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        UUID charger = UUID.randomUUID();
        when(catalog.activeProducts()).thenReturn(List.of(new CatalogClient.CatalogProduct(charger, "Dual-Port Car Charger", "Fast USB charger", new BigDecimal("799"), "INR", "Travel tech", null)));
        when(openAi.understand(anyString(), anyString())).thenReturn(java.util.Optional.empty()); when(customer.profileSummary(any(), anyString())).thenReturn("");

        AiChatResponse response = new AiChatService(catalog, customer, openAi).chat(UUID.randomUUID(), "Bearer token", new AiChatRequest("INR 800", null, null, null, null, null, "morning", List.of(), List.of(new ConversationTurn("user", "I am going to the airport"))));

        assertFalse(response.recommendedProducts().isEmpty()); assertEquals(charger, response.recommendedProducts().getFirst().id()); verify(catalog).activeProducts();
    }

    @Test void excludesProductsOutsideAssignedCabInventory() {
        CatalogClient catalog = mock(CatalogClient.class); CustomerClient customer = mock(CustomerClient.class); OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        UUID available = UUID.randomUUID(); UUID unavailable = UUID.randomUUID();
        when(catalog.activeProducts()).thenReturn(List.of(
                new CatalogClient.CatalogProduct(available, "Car Charger", "Fast USB charger", new BigDecimal("799"), "INR", "Travel tech", null),
                new CatalogClient.CatalogProduct(unavailable, "USB Cable", "Charging cable", new BigDecimal("399"), "INR", "Travel tech", null)));
        when(openAi.understand(anyString(), anyString())).thenReturn(java.util.Optional.empty()); when(customer.profileSummary(any(), anyString())).thenReturn("");

        AiChatResponse response = new AiChatService(catalog, customer, openAi).chat(UUID.randomUUID(), "Bearer token", new AiChatRequest("I need a charger", null, null, null, null, null, "morning", List.of(available), List.of()));

        assertEquals(1, response.recommendedProducts().size()); assertEquals(available, response.recommendedProducts().getFirst().id());
    }

    @Test void recommendsExpandedCabInventoryForAChipsRequest() {
        CatalogClient catalog = mock(CatalogClient.class); CustomerClient customer = mock(CustomerClient.class); OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        UUID chips = UUID.randomUUID(); UUID charger = UUID.randomUUID();
        when(catalog.activeProducts()).thenReturn(List.of(
                new CatalogClient.CatalogProduct(chips, "Sea Salt Potato Chips", "Crisp snack pack", new BigDecimal("85"), "INR", "Chips", null),
                new CatalogClient.CatalogProduct(charger, "Phone Charger", "Fast charging", new BigDecimal("899"), "INR", "Phone Chargers", null)));
        when(openAi.understand(anyString(), anyString())).thenReturn(java.util.Optional.empty()); when(customer.profileSummary(any(), anyString())).thenReturn("");

        AiChatResponse response = new AiChatService(catalog, customer, openAi).chat(UUID.randomUUID(), "Bearer token", new AiChatRequest("I want chips", null, null, null, null, null, "afternoon", List.of(chips), List.of()));

        assertEquals(1, response.recommendedProducts().size()); assertEquals(chips, response.recommendedProducts().getFirst().id());
    }

    @Test void groundsEachRequestInTheCurrentCatalogIntent() {
        CatalogClient catalog = mock(CatalogClient.class);
        CustomerClient customer = mock(CustomerClient.class);
        OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        CatalogClient.CatalogProduct chips = product("Sea Salt Potato Chips", "Crisp snack pack", "Chips", "85");
        CatalogClient.CatalogProduct proteinBar = product("Almond Protein Bar", "Chocolate protein snack", "Protein Bars", "129");
        CatalogClient.CatalogProduct coffee = product("Instant Coffee Sachet", "Single-serve coffee", "Instant Coffee", "45");
        CatalogClient.CatalogProduct charger = product("20W Fast Phone Charger", "Reliable battery top-up", "Phone Chargers", "899");
        CatalogClient.CatalogProduct cable = product("USB-C Fast Charging Cable", "Cable for charging", "Charging Cables", "349");
        CatalogClient.CatalogProduct eyeMask = product("Satin Eye Mask", "Restful journey comfort", "Eye Masks", "179");
        CatalogClient.CatalogProduct motion = product("Motion Ease Tablets (Mock)", "Demo-only motion comfort item", "Motion Sickness Tablets (Mock)", "99");
        CatalogClient.CatalogProduct relief = product("Muscle Relief Spray", "Topical comfort for tired muscles", "Pain Relief Spray", "249");
        CatalogClient.CatalogProduct earphones = product("Wired Travel Earphones", "Calls and music", "Earphones", "499");
        CatalogClient.CatalogProduct water = product("Mineral Water", "Sealed bottle", "Water", "40");
        CatalogClient.CatalogProduct pillow = product("Soft Travel Neck Pillow", "Airport transfer comfort", "Neck Pillows", "799");
        when(catalog.activeProducts()).thenReturn(List.of(chips, proteinBar, coffee, charger, cable, eyeMask, motion, relief, earphones, water, pillow));
        when(openAi.understand(anyString(), anyString())).thenReturn(java.util.Optional.empty());

        AiChatService service = new AiChatService(catalog, customer, openAi);

        AiChatResponse snacks = chat(service, "snacks");
        assertFalse(snacks.readyForRecommendations());
        assertTrue(snacks.assistantMessage().contains("What type of snack"));

        assertOnlyRecommendation(chat(service, "protein bar"), proteinBar.id());
        assertOnlyRecommendation(chat(service, "coffee"), coffee.id());
        assertTrue(chat(service, "charger").recommendedProducts().stream().anyMatch(product -> product.id().equals(charger.id())));
        assertOnlyRecommendation(chat(service, "face mask", List.of(new ConversationTurn("user", "I want snacks"))), eyeMask.id());
        assertTrue(chat(service, "face mask").assistantMessage().contains("do not see a face mask"));
        assertTrue(chat(service, "medicine").recommendedProducts().stream().allMatch(product -> product.id().equals(motion.id()) || product.id().equals(relief.id())));
        assertOnlyRecommendation(chat(service, "headphones"), earphones.id());
        assertOnlyRecommendation(chat(service, "water"), water.id());

        AiChatResponse airport = chat(service, "airport essentials");
        assertTrue(airport.readyForRecommendations());
        assertTrue(airport.recommendedProducts().stream().noneMatch(product -> product.id().equals(chips.id())));
        assertTrue(airport.recommendedProducts().stream().anyMatch(product -> product.id().equals(pillow.id())));
        assertTrue(airport.recommendedProducts().stream().anyMatch(product -> product.id().equals(charger.id())));
    }

    private AiChatResponse chat(AiChatService service, String message) {
        return chat(service, message, List.of());
    }

    private AiChatResponse chat(AiChatService service, String message, List<ConversationTurn> conversation) {
        return service.chat(UUID.randomUUID(), "Bearer token", new AiChatRequest(message, null, null, null, null, null, "afternoon", List.of(), conversation));
    }

    private void assertOnlyRecommendation(AiChatResponse response, UUID expectedProductId) {
        assertEquals(1, response.recommendedProducts().size());
        assertEquals(expectedProductId, response.recommendedProducts().getFirst().id());
    }

    private CatalogClient.CatalogProduct product(String name, String description, String category, String price) {
        return new CatalogClient.CatalogProduct(UUID.randomUUID(), name, description, new BigDecimal(price), "INR", category, null);
    }
}
