package com.weatherbroadcast.service;

import com.google.firebase.messaging.*;
import com.weatherbroadcast.dto.PushNotificationRequest;
import com.weatherbroadcast.dto.PushNotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servizio per l'invio di notifiche push tramite Firebase Cloud Messaging (FCM)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    /**
     * Invia una notifica push a un singolo dispositivo tramite token FCM
     *
     * @param request dati della notifica
     * @return risposta con esito dell'invio
     */
    public PushNotificationResponse sendToDevice(PushNotificationRequest request) {
        if (request.getToken() == null || request.getToken().isEmpty()) {
            log.error("Token FCM mancante");
            return PushNotificationResponse.failure("Token FCM è obbligatorio");
        }

        try {
            Message message = buildMessage(request, request.getToken(), null);
            String messageId = FirebaseMessaging.getInstance().send(message);

            log.info("Notifica inviata con successo. Message ID: {}", messageId);
            return PushNotificationResponse.success(
                    messageId,
                    "Notifica inviata al dispositivo");

        } catch (FirebaseMessagingException e) {
            log.error("Errore nell'invio della notifica al dispositivo: {}", e.getMessage(), e);
            return PushNotificationResponse.failure(
                    "Errore nell'invio: " + e.getMessage());
        }
    }

    /**
     * Invia una notifica push a tutti i dispositivi iscritti a un topic
     *
     * @param request dati della notifica
     * @return risposta con esito dell'invio
     */
    public PushNotificationResponse sendToTopic(PushNotificationRequest request) {
        if (request.getTopic() == null || request.getTopic().isEmpty()) {
            log.error("Topic mancante");
            return PushNotificationResponse.failure("Topic è obbligatorio");
        }

        try {
            Message message = buildMessage(request, null, request.getTopic());
            String messageId = FirebaseMessaging.getInstance().send(message);

            log.info("Notifica inviata al topic '{}'. Message ID: {}", request.getTopic(), messageId);
            return PushNotificationResponse.success(
                    messageId,
                    "Notifica inviata al topic: " + request.getTopic());

        } catch (FirebaseMessagingException e) {
            log.error("Errore nell'invio della notifica al topic {}: {}",
                    request.getTopic(), e.getMessage(), e);
            return PushNotificationResponse.failure(
                    "Errore nell'invio al topic: " + e.getMessage());
        }
    }

    /**
     * Invia una notifica broadcast a tutti gli utenti
     * Utilizza il topic "all" che dovrebbe essere configurato nel frontend
     *
     * @param request dati della notifica
     * @return risposta con esito dell'invio
     */
    public PushNotificationResponse sendBroadcast(PushNotificationRequest request) {
        request.setTopic("all");
        return sendToTopic(request);
    }

    /**
     * Costruisce il messaggio Firebase da inviare
     *
     * @param request dati della notifica
     * @param token   token FCM del dispositivo (null per topic)
     * @param topic   topic FCM (null per singolo dispositivo)
     * @return messaggio Firebase pronto per l'invio
     */
    private Message buildMessage(PushNotificationRequest request, String token, String topic) {
        // Costruisci la notifica visiva
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody());

        if (request.getIcon() != null) {
            notificationBuilder.setImage(request.getIcon());
        }

        if (request.getImage() != null) {
            notificationBuilder.setImage(request.getImage());
        }

        // Costruisci i dati custom
        Map<String, String> data = new HashMap<>();
        if (request.getData() != null) {
            data.putAll(request.getData());
        }

        // Aggiungi URL di click se presente
        if (request.getClickUrl() != null) {
            data.put("clickUrl", request.getClickUrl());
        }

        // Aggiungi tag se presente
        if (request.getTag() != null) {
            data.put("tag", request.getTag());
        }

        // Configurazione Web Push
        WebpushConfig webpushConfig = WebpushConfig.builder()
                .setNotification(WebpushNotification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .setIcon(request.getIcon() != null ? request.getIcon() : "/icons/icon-192x192.png")
                        .setImage(request.getImage())
                        .setTag(request.getTag())
                        .setRequireInteraction(false)
                        .build())
                .setFcmOptions(WebpushFcmOptions.builder()
                        .setLink(request.getClickUrl() != null ? request.getClickUrl() : "/")
                        .build())
                .build();

        // Configurazione Android (opzionale)
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(request.getPriority().equalsIgnoreCase("high") ? AndroidConfig.Priority.HIGH
                        : AndroidConfig.Priority.NORMAL)
                .setNotification(AndroidNotification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .setIcon(request.getIcon())
                        .setTag(request.getTag())
                        .build())
                .build();

        // Costruisci il messaggio finale
        Message.Builder messageBuilder = Message.builder()
                .setNotification(notificationBuilder.build())
                .putAllData(data)
                .setWebpushConfig(webpushConfig)
                .setAndroidConfig(androidConfig);

        // Imposta target (token o topic)
        if (token != null) {
            messageBuilder.setToken(token);
        } else if (topic != null) {
            messageBuilder.setTopic(topic);
        }

        return messageBuilder.build();
    }

    /**
     * Iscrive un dispositivo a un topic
     *
     * @param token token FCM del dispositivo
     * @param topic nome del topic
     * @return true se l'iscrizione ha successo
     */
    public boolean subscribeToTopic(String token, String topic) {
        try {
            FirebaseMessaging.getInstance()
                    .subscribeToTopic(java.util.List.of(token), topic);
            log.info("Token {} iscritto al topic {}", token, topic);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("Errore nell'iscrizione al topic {}: {}", topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Disiscrive un dispositivo da un topic
     *
     * @param token token FCM del dispositivo
     * @param topic nome del topic
     * @return true se la disiscrizione ha successo
     */
    public boolean unsubscribeFromTopic(String token, String topic) {
        try {
            FirebaseMessaging.getInstance()
                    .unsubscribeFromTopic(java.util.List.of(token), topic);
            log.info("Token {} disiscritto dal topic {}", token, topic);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("Errore nella disiscrizione dal topic {}: {}", topic, e.getMessage(), e);
            return false;
        }
    }
}
