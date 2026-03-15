package com.weatherbroadcast.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Scope destinatari disponibili per la newsletter.
 */
public enum NewsletterRecipientScope {
    ADMINS("admins"),
    ALL_USERS("all-users");

    private final String value;

    NewsletterRecipientScope(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NewsletterRecipientScope fromValue(String value) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim().toLowerCase();
        for (NewsletterRecipientScope scope : values()) {
            if (scope.value.equals(normalizedValue)) {
                return scope;
            }
        }

        throw new IllegalArgumentException("Scope destinatari non valido: " + value);
    }
}