package com.inridemart.ai;

import java.util.Arrays;

enum TravelLanguage {
    ENGLISH("English"),
    HINDI("Hindi"),
    KANNADA("Kannada"),
    TELUGU("Telugu"),
    TAMIL("Tamil"),
    MALAYALAM("Malayalam"),
    MARATHI("Marathi"),
    BENGALI("Bengali"),
    GUJARATI("Gujarati"),
    PUNJABI("Punjabi"),
    URDU("Urdu"),
    SPANISH("Spanish"),
    FRENCH("French"),
    GERMAN("German"),
    PORTUGUESE("Portuguese"),
    ITALIAN("Italian"),
    DUTCH("Dutch"),
    RUSSIAN("Russian"),
    ARABIC("Arabic"),
    TURKISH("Turkish"),
    CHINESE("Chinese"),
    JAPANESE("Japanese"),
    KOREAN("Korean"),
    THAI("Thai"),
    VIETNAMESE("Vietnamese"),
    INDONESIAN("Indonesian"),
    UNKNOWN("Unsupported");

    private final String displayName;

    TravelLanguage(String displayName) {
        this.displayName = displayName;
    }

    String displayName() {
        return displayName;
    }

    boolean supported() {
        return this != UNKNOWN;
    }

    static TravelLanguage fromLabel(String value) {
        if (value == null || value.isBlank()) return UNKNOWN;
        return Arrays.stream(values())
                .filter(language -> language.displayName.equalsIgnoreCase(value) || language.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
