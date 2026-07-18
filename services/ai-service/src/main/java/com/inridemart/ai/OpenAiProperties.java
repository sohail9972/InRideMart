package com.inridemart.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("openai")
public record OpenAiProperties(String apiKey, String model, double temperature, int maxOutputTokens) {
    public boolean enabled() { return apiKey != null && !apiKey.isBlank() && model != null && !model.isBlank(); }
}
