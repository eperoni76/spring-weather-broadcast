package com.weatherbroadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO di risposta per l'invio newsletter.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterResponse {

    /**
     * Indica se almeno una mail è stata inviata.
     */
    private boolean success;

    /**
     * Messaggio descrittivo dell'esito.
     */
    private String message;

    /**
     * Dettagli errore, valorizzati solo in caso di fallimento.
     */
    private String error;

    /**
     * Numero di destinatari raggiunti con successo.
     */
    private Integer recipientCount;

    /**
     * Numero di destinatari scartati o falliti.
     */
    private Integer failedRecipientCount;

    /**
     * Scope usato per l'invio.
     */
    private NewsletterRecipientScope recipientScope;

    public static NewsletterResponse success(
            String message,
            NewsletterRecipientScope recipientScope,
            int recipientCount,
            int failedRecipientCount) {

        return NewsletterResponse.builder()
                .success(true)
                .message(message)
                .recipientScope(recipientScope)
                .recipientCount(recipientCount)
                .failedRecipientCount(failedRecipientCount)
                .build();
    }

    public static NewsletterResponse failure(String error, NewsletterRecipientScope recipientScope) {
        return NewsletterResponse.builder()
                .success(false)
                .error(error)
                .recipientScope(recipientScope)
                .build();
    }
}