package com.weatherbroadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta dopo l'invio di una notifica push
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationResponse {

    /**
     * Indica se l'invio è andato a buon fine
     */
    private boolean success;

    /**
     * Messaggio di risposta
     */
    private String message;

    /**
     * ID del messaggio Firebase (se successo)
     */
    private String messageId;

    /**
     * Dettagli dell'errore (se fallito)
     */
    private String error;

    /**
     * Numero di destinatari raggiunti (per topic o broadcast)
     */
    private Integer recipientCount;

    public static PushNotificationResponse success(String messageId, String message) {
        return PushNotificationResponse.builder()
                .success(true)
                .messageId(messageId)
                .message(message)
                .build();
    }

    public static PushNotificationResponse failure(String error) {
        return PushNotificationResponse.builder()
                .success(false)
                .error(error)
                .build();
    }
}
