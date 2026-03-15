package com.weatherbroadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la composizione di una newsletter inviata da un admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterRequest {

    /**
     * Oggetto dell'email.
     */
    private String subject;

    /**
     * Titolo principale mostrato nel template HTML.
     */
    private String title;

    /**
     * Introduzione opzionale mostrata sopra al contenuto principale.
     */
    private String intro;

    /**
     * Corpo principale della newsletter.
     */
    private String body;

    /**
     * Etichetta opzionale del bottone CTA.
     */
    private String ctaLabel;

    /**
     * URL opzionale associato alla CTA.
     */
    private String ctaUrl;

    /**
     * Nota finale opzionale nel footer della mail.
     */
    private String footerNote;

    /**
     * Pubblico destinatario della newsletter.
     */
    private NewsletterRecipientScope recipientScope;
}