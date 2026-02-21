package com.weatherbroadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO per l'invio di notifiche push tramite Firebase Cloud Messaging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {

    /**
     * Titolo della notifica
     */
    private String title;

    /**
     * Corpo del messaggio
     */
    private String body;

    /**
     * URL dell'icona (opzionale)
     */
    private String icon;

    /**
     * URL dell'immagine (opzionale)
     */
    private String image;

    /**
     * Tag per raggruppare notifiche (opzionale)
     */
    private String tag;

    /**
     * URL a cui navigare al click (opzionale)
     */
    private String clickUrl;

    /**
     * Dati aggiuntivi custom (opzionale)
     */
    private Map<String, String> data;

    /**
     * Token FCM del dispositivo target (se si invia a un singolo utente)
     */
    private String token;

    /**
     * Topic FCM per l'invio broadcast (se si invia a un topic)
     */
    private String topic;

    /**
     * Priorità della notifica (high, normal)
     */
    @Builder.Default
    private String priority = "high";
}
