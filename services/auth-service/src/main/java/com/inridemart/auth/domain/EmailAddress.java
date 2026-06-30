package com.inridemart.auth.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 320 || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email is invalid");
        }
    }
}

