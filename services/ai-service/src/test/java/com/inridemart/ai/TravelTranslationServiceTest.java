package com.inridemart.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TravelTranslationServiceTest {
    @Test
    void detectsJapaneseAndTranslatesTheKnownChargerQuestionForAKannadaDriver() {
        OpenAiResponsesClient openAi = unavailableClient();

        TravelTranslationResponse response = new TravelTranslationService(openAi)
                .translate(new TravelTranslationRequest("\u30b9\u30de\u30db\u306e\u5145\u96fb\u5668\u306f\u3042\u308a\u307e\u3059\u304b\uff1f", TravelLanguage.KANNADA, null));

        assertEquals(TravelLanguage.JAPANESE, response.passengerLanguage());
        assertEquals("\u0cae\u0cca\u0cac\u0cc8\u0cb2\u0ccd \u0c9a\u0cbe\u0cb0\u0ccd\u0c9c\u0cb0\u0ccd \u0c87\u0ca6\u0cc6\u0caf\u0cc7?", response.driverTranslation());
        assertTrue(response.supported());
        assertTrue(response.fallbackUsed());
        assertEquals("OpenAI API returned HTTP 429", response.fallbackReason());
    }

    @Test
    void translatesTheKannadaDriverReplyBackToTheDetectedPassengerLanguage() {
        OpenAiResponsesClient openAi = unavailableClient();

        TravelTranslationResponse response = new TravelTranslationService(openAi)
                .translate(new TravelTranslationRequest(
                        "\u30b9\u30de\u30db\u306e\u5145\u96fb\u5668\u306f\u3042\u308a\u307e\u3059\u304b\uff1f",
                        TravelLanguage.KANNADA,
                        "\u0cb9\u0ccc\u0ca6\u0cc1, \u0c95\u0cbe\u0cb0\u0cbf\u0ca8\u0cb2\u0ccd\u0cb2\u0cbf \u0c87\u0ca6\u0cc6."));

        assertEquals("\u306f\u3044\u3001\u8eca\u5185\u306b\u3042\u308a\u307e\u3059\u3002", response.passengerTranslation());
        assertTrue(response.supported());
    }

    @Test
    void translatesGreetingAndDriverReplyWhenOpenAiIsUnavailable() {
        TravelTranslationService service = new TravelTranslationService(unavailableClient());

        TravelTranslationResponse response = service.translate(new TravelTranslationRequest(
                "hello", TravelLanguage.KANNADA, "\u0ca8\u0cae\u0cb8\u0ccd\u0c95\u0cbe\u0cb0"));

        assertEquals(TravelLanguage.ENGLISH, response.passengerLanguage());
        assertEquals("\u0ca8\u0cae\u0cb8\u0ccd\u0c95\u0cbe\u0cb0", response.driverTranslation());
        assertEquals("Hello", response.passengerTranslation());
        assertTrue(response.fallbackUsed());
    }

    @Test
    void usesTheMockedOpenAiDecisionWhenAvailable() {
        OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        when(openAi.translate(any())).thenReturn(OpenAiResponsesClient.TranslationAttempt.success(
                new OpenAiResponsesClient.TranslationDecision(
                        TravelLanguage.SPANISH,
                        TravelLanguage.HINDI,
                        "\u0915\u0943\u092a\u092f\u093e \u092f\u0939\u093e\u0902 \u0930\u0941\u0915\u093f\u090f\u0964",
                        "Puede parar aqui.",
                        true)));

        TravelTranslationResponse response = new TravelTranslationService(openAi)
                .translate(new TravelTranslationRequest("Puede parar aqui?", TravelLanguage.HINDI, null));

        assertEquals(TravelLanguage.SPANISH, response.passengerLanguage());
        assertEquals("\u0915\u0943\u092a\u092f\u093e \u092f\u0939\u093e\u0902 \u0930\u0941\u0915\u093f\u090f\u0964", response.driverTranslation());
        assertFalse(response.fallbackUsed());
        assertNull(response.fallbackReason());
    }

    @Test
    void reportsUnsupportedScriptsGracefullyWhenTheModelIsUnavailable() {
        OpenAiResponsesClient openAi = unavailableClient();

        TravelTranslationResponse response = new TravelTranslationService(openAi)
                .translate(new TravelTranslationRequest("\ua9dc\ua9dc", TravelLanguage.KANNADA, null));

        assertEquals(TravelLanguage.UNKNOWN, response.passengerLanguage());
        assertFalse(response.supported());
        assertEquals("\ua9dc\ua9dc", response.driverTranslation());
    }

    private OpenAiResponsesClient unavailableClient() {
        OpenAiResponsesClient openAi = mock(OpenAiResponsesClient.class);
        when(openAi.translate(any())).thenReturn(OpenAiResponsesClient.TranslationAttempt.unavailable("OpenAI API returned HTTP 429"));
        return openAi;
    }
}
