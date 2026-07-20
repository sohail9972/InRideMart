package com.inridemart.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
class TravelTranslationService {
    private static final Logger log = LoggerFactory.getLogger(TravelTranslationService.class);
    private final OpenAiResponsesClient openAi;

    TravelTranslationService(OpenAiResponsesClient openAi) {
        this.openAi = openAi;
    }

    TravelTranslationResponse translate(TravelTranslationRequest request) {
        TravelLanguage driverLanguage = request.effectiveDriverLanguage();
        String passengerMessage = request.passengerMessage().trim();
        String driverReply = request.driverReply() == null ? "" : request.driverReply().trim();
        OpenAiResponsesClient.TranslationAttempt attempt = openAi.translate(
                new OpenAiResponsesClient.TranslationPrompt(passengerMessage, driverLanguage, driverReply));

        if (attempt.usedOpenAi()) {
            OpenAiResponsesClient.TranslationDecision decision = attempt.decision();
            log.info("Passenger-driver translation served by GPT-5.6: {} to {}.", decision.passengerLanguage(), driverLanguage);
            return new TravelTranslationResponse(
                    passengerMessage,
                    decision.passengerLanguage(),
                    driverLanguage,
                    decision.driverTranslation(),
                    driverReply,
                    decision.passengerTranslation(),
                    decision.supported() && decision.passengerLanguage().supported() && driverLanguage.supported(),
                    false,
                    null);
        }

        return fallback(passengerMessage, driverLanguage, driverReply, attempt.fallbackReason());
    }

    private TravelTranslationResponse fallback(String passengerMessage, TravelLanguage driverLanguage,
                                               String driverReply, String reason) {
        log.warn("Passenger-driver translation served by local fallback: {}", reason);
        TravelLanguage passengerLanguage = detect(passengerMessage);
        boolean supported = passengerLanguage.supported() && driverLanguage.supported();
        String driverTranslation = knownPassengerTranslation(passengerMessage, driverLanguage);
        String passengerTranslation = driverReply.isBlank() ? "" : knownDriverTranslation(driverReply, passengerLanguage);
        return new TravelTranslationResponse(
                passengerMessage,
                passengerLanguage,
                driverLanguage,
                driverTranslation,
                driverReply,
                passengerTranslation,
                supported,
                true,
                reason);
    }

    private TravelLanguage detect(String text) {
        if (text.matches(".*[\\u3040-\\u30ff].*")) return TravelLanguage.JAPANESE;
        if (text.matches(".*[\\uac00-\\ud7af].*")) return TravelLanguage.KOREAN;
        if (text.matches(".*[\\u0c80-\\u0cff].*")) return TravelLanguage.KANNADA;
        if (text.matches(".*[\\u0c00-\\u0c7f].*")) return TravelLanguage.TELUGU;
        if (text.matches(".*[\\u0b80-\\u0bff].*")) return TravelLanguage.TAMIL;
        if (text.matches(".*[\\u0d00-\\u0d7f].*")) return TravelLanguage.MALAYALAM;
        if (text.matches(".*[\\u0980-\\u09ff].*")) return TravelLanguage.BENGALI;
        if (text.matches(".*[\\u0a80-\\u0aff].*")) return TravelLanguage.GUJARATI;
        if (text.matches(".*[\\u0a00-\\u0a7f].*")) return TravelLanguage.PUNJABI;
        if (text.matches(".*[\\u0900-\\u097f].*")) return TravelLanguage.HINDI;
        if (text.matches(".*[\\u0400-\\u04ff].*")) return TravelLanguage.RUSSIAN;
        if (text.matches(".*[\\u0600-\\u06ff].*")) return TravelLanguage.ARABIC;
        if (text.matches(".*[\\u0e00-\\u0e7f].*")) return TravelLanguage.THAI;
        if (text.matches(".*[\\u4e00-\\u9fff].*")) return TravelLanguage.CHINESE;
        if (!text.matches(".*[A-Za-z].*")) return TravelLanguage.UNKNOWN;

        String normalized = text.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "hola", "puede", "gracias", "aqui")) return TravelLanguage.SPANISH;
        if (containsAny(normalized, "bonjour", "merci", "arreter")) return TravelLanguage.FRENCH;
        if (containsAny(normalized, "hallo", "danke", "bitte")) return TravelLanguage.GERMAN;
        if (containsAny(normalized, "ol\u00e1", "obrigado", "voce")) return TravelLanguage.PORTUGUESE;
        if (containsAny(normalized, "ciao", "grazie")) return TravelLanguage.ITALIAN;
        if (containsAny(normalized, "merhaba", "te\u015fekk\u00fcr")) return TravelLanguage.TURKISH;
        if (containsAny(normalized, "halo", "terima kasih")) return TravelLanguage.INDONESIAN;
        if (containsAny(normalized, "xin chao", "cam on")) return TravelLanguage.VIETNAMESE;
        return TravelLanguage.ENGLISH;
    }

    private String knownPassengerTranslation(String message, TravelLanguage driverLanguage) {
        String normalized = message.toLowerCase(Locale.ROOT);
        if (isGreeting(normalized)) return greeting(driverLanguage, message);
        if (containsAny(normalized, "charger", "\u5145\u96fb\u5668")) return chargerQuestion(driverLanguage, message);
        if (containsAny(normalized, "stop", "parar", "\u0930\u0941\u0915", "\u0ca8\u0cbf\u0cb2\u0ccd\u0cb2")) return stopRequest(driverLanguage, message);
        if (containsAny(normalized, "air conditioning", "ac", "\u0c8e\u0cb8\u0cbf")) return acRequest(driverLanguage, message);
        if (containsAny(normalized, "luggage", "bag", "baggage")) return luggageRequest(driverLanguage, message);
        return message;
    }

    private String knownDriverTranslation(String message, TravelLanguage passengerLanguage) {
        String normalized = message.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "\u0ca8\u0cbf\u0cae\u0c97\u0cc6 \u0cb9\u0cc7\u0c97\u0cc6 \u0cb8\u0cb9\u0cbe\u0caf \u0cae\u0cbe\u0ca1\u0cac\u0cb9\u0cc1\u0ca6\u0cc7")) {
            return helpOffer(passengerLanguage, message);
        }
        if (isGreeting(normalized)) return greeting(passengerLanguage, message);
        if (containsAny(normalized, "\u0c95\u0cbe\u0cb0\u0cbf\u0ca8\u0cb2\u0ccd\u0cb2\u0cbf \u0c87\u0ca6\u0cc6", "charger is available", "cargador disponible")) {
            return chargerAvailable(passengerLanguage, message);
        }
        return message;
    }

    private boolean isGreeting(String text) {
        return text.matches("^(hi|hello|hey)[!.?\\s]*$")
                || containsAny(text, "\u0ca8\u0cae\u0cb8\u0ccd\u0c95\u0cbe\u0cb0", "\u0928\u092e\u0938\u094d\u0924\u0947", "\u3053\u3093\u306b\u3061\u306f", "hola", "bonjour");
    }

    private String greeting(TravelLanguage target, String original) {
        return switch (target) {
            case KANNADA -> "\u0ca8\u0cae\u0cb8\u0ccd\u0c95\u0cbe\u0cb0";
            case HINDI -> "\u0928\u092e\u0938\u094d\u0924\u0947";
            case JAPANESE -> "\u3053\u3093\u306b\u3061\u306f";
            case SPANISH -> "Hola";
            case FRENCH -> "Bonjour";
            case ENGLISH -> "Hello";
            default -> original;
        };
    }

    private String helpOffer(TravelLanguage target, String original) {
        return switch (target) {
            case ENGLISH -> "How can I help you?";
            case HINDI -> "\u092e\u0948\u0902 \u0906\u092a\u0915\u0940 \u0915\u0948\u0938\u0947 \u092e\u0926\u0926 \u0915\u0930 \u0938\u0915\u0924\u093e \u0939\u0942\u0902?";
            case JAPANESE -> "\u3069\u306e\u3088\u3046\u306b\u304a\u624b\u4f1d\u3044\u3067\u304d\u307e\u3059\u304b\uff1f";
            case SPANISH -> "Como puedo ayudarle?";
            default -> original;
        };
    }

    private String chargerQuestion(TravelLanguage target, String original) {
        return switch (target) {
            case KANNADA -> "\u0cae\u0cca\u0cac\u0cc8\u0cb2\u0ccd \u0c9a\u0cbe\u0cb0\u0ccd\u0c9c\u0cb0\u0ccd \u0c87\u0ca6\u0cc6\u0caf\u0cc7?";
            case HINDI -> "\u0915\u094d\u092f\u093e \u0906\u092a\u0915\u0947 \u092a\u093e\u0938 \u092b\u094b\u0928 \u091a\u093e\u0930\u094d\u091c\u0930 \u0939\u0948?";
            case JAPANESE -> "\u30b9\u30de\u30db\u306e\u5145\u96fb\u5668\u306f\u3042\u308a\u307e\u3059\u304b\uff1f";
            case SPANISH -> "\u00bfTiene un cargador de tel\u00e9fono?";
            case ENGLISH -> "Do you have a phone charger?";
            default -> original;
        };
    }

    private String chargerAvailable(TravelLanguage target, String original) {
        return switch (target) {
            case JAPANESE -> "\u306f\u3044\u3001\u8eca\u5185\u306b\u3042\u308a\u307e\u3059\u3002";
            case KANNADA -> "\u0cb9\u0ccc\u0ca6\u0cc1, \u0c95\u0cbe\u0cb0\u0cbf\u0ca8\u0cb2\u0ccd\u0cb2\u0cbf \u0c87\u0ca6\u0cc6.";
            case HINDI -> "\u0939\u093e\u0902, \u0915\u093e\u0930 \u092e\u0947\u0902 \u0909\u092a\u0932\u092c\u094d\u0927 \u0939\u0948\u0964";
            case SPANISH -> "S\u00ed, hay un cargador disponible en el coche.";
            case ENGLISH -> "Yes, a charger is available in the cab.";
            default -> original;
        };
    }

    private String stopRequest(TravelLanguage target, String original) {
        return switch (target) {
            case KANNADA -> "\u0ca6\u0caf\u0cb5\u0cbf\u0c9f\u0ccd\u0c9f\u0cc1 \u0c87\u0cb2\u0ccd\u0cb2\u0cbf \u0ca8\u0cbf\u0cb2\u0ccd\u0cb2\u0cbf\u0cb8\u0cbf.";
            case HINDI -> "\u0915\u0943\u092a\u092f\u093e \u092f\u0939\u093e\u0902 \u0930\u0941\u0915\u093f\u090f\u0964";
            case SPANISH -> "Por favor, pare aqui.";
            case ENGLISH -> "Please stop here.";
            default -> original;
        };
    }

    private String acRequest(TravelLanguage target, String original) {
        return switch (target) {
            case KANNADA -> "\u0ca6\u0caf\u0cb5\u0cbf\u0c9f\u0ccd\u0c9f\u0cc1 \u0c8e\u0cb8\u0cbf \u0c86\u0ca8\u0ccd \u0cae\u0cbe\u0ca1\u0cac\u0cb9\u0cc1\u0ca6\u0cc7?";
            case HINDI -> "\u0915\u094d\u092f\u093e \u0906\u092a \u090f\u0938\u0940 \u091a\u093e\u0932\u0942 \u0915\u0930 \u0938\u0915\u0924\u0947 \u0939\u0948\u0902?";
            case ENGLISH -> "Could you turn on the AC?";
            default -> original;
        };
    }

    private String luggageRequest(TravelLanguage target, String original) {
        return switch (target) {
            case KANNADA -> "\u0ca6\u0caf\u0cb5\u0cbf\u0c9f\u0ccd\u0c9f\u0cc1 \u0ca8\u0ca8\u0ccd\u0ca8 \u0cb8\u0cbe\u0cae\u0cbe\u0ca8\u0cc1\u0c97\u0cb3\u0ccb\u0c82\u0ca6\u0cbf\u0c97\u0cc6 \u0cb8\u0cb9\u0cbe\u0caf \u0cae\u0cbe\u0ca1\u0cac\u0cb9\u0cc1\u0ca6\u0cc7?";
            case HINDI -> "\u0915\u0943\u092a\u092f\u093e \u092e\u0947\u0930\u0947 \u0938\u093e\u092e\u093e\u0928 \u092e\u0947\u0902 \u092e\u0926\u0926 \u0915\u0930 \u0938\u0915\u0924\u0947 \u0939\u0948\u0902?";
            case ENGLISH -> "Could you help with my luggage?";
            default -> original;
        };
    }

    private boolean containsAny(String source, String... terms) {
        for (String term : terms) if (source.contains(term)) return true;
        return false;
    }
}
