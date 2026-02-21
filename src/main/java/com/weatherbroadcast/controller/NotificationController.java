package com.weatherbroadcast.controller;

import com.weatherbroadcast.dto.PushNotificationRequest;
import com.weatherbroadcast.dto.PushNotificationResponse;
import com.weatherbroadcast.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST per l'invio di notifiche push
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final PushNotificationService pushNotificationService;

    /**
     * Invia una notifica a un singolo dispositivo tramite token FCM
     *
     * POST /api/notifications/send
     * Body: PushNotificationRequest con token
     */
    @PostMapping("/send")
    public ResponseEntity<PushNotificationResponse> sendToDevice(
            @RequestBody PushNotificationRequest request) {

        log.info("Richiesta invio notifica a dispositivo: {}", request.getTitle());
        PushNotificationResponse response = pushNotificationService.sendToDevice(request);

        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    /**
     * Invia una notifica a tutti i dispositivi iscritti a un topic
     *
     * POST /api/notifications/send-topic
     * Body: PushNotificationRequest con topic
     */
    @PostMapping("/send-topic")
    public ResponseEntity<PushNotificationResponse> sendToTopic(
            @RequestBody PushNotificationRequest request) {

        log.info("Richiesta invio notifica al topic: {}", request.getTopic());
        PushNotificationResponse response = pushNotificationService.sendToTopic(request);

        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    /**
     * Invia una notifica broadcast a tutti gli utenti
     *
     * POST /api/notifications/broadcast
     * Body: PushNotificationRequest (senza token o topic)
     */
    @PostMapping("/broadcast")
    public ResponseEntity<PushNotificationResponse> sendBroadcast(
            @RequestBody PushNotificationRequest request) {

        log.info("Richiesta invio notifica broadcast: {}", request.getTitle());
        PushNotificationResponse response = pushNotificationService.sendBroadcast(request);

        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    /**
     * Iscrive un dispositivo a un topic
     *
     * POST /api/notifications/subscribe
     * Body: { "token": "...", "topic": "..." }
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribeToTopic(
            @RequestBody Map<String, String> request) {

        String token = request.get("token");
        String topic = request.get("topic");

        if (token == null || topic == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Token e topic sono obbligatori"));
        }

        boolean success = pushNotificationService.subscribeToTopic(token, topic);

        return success ? ResponseEntity.ok(Map.of("success", true, "message", "Iscritto al topic: " + topic))
                : ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Errore nell'iscrizione al topic"));
    }

    /**
     * Disiscrive un dispositivo da un topic
     *
     * POST /api/notifications/unsubscribe
     * Body: { "token": "...", "topic": "..." }
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribeFromTopic(
            @RequestBody Map<String, String> request) {

        String token = request.get("token");
        String topic = request.get("topic");

        if (token == null || topic == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Token e topic sono obbligatori"));
        }

        boolean success = pushNotificationService.unsubscribeFromTopic(token, topic);

        return success ? ResponseEntity.ok(Map.of("success", true, "message", "Disiscritto dal topic: " + topic))
                : ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Errore nella disiscrizione dal topic"));
    }

    /**
     * Endpoint per testare l'invio di notifiche
     *
     * POST /api/notifications/test
     * Body: { "token": "..." } opzionale
     */
    @PostMapping("/test")
    public ResponseEntity<PushNotificationResponse> sendTestNotification(
            @RequestBody(required = false) Map<String, String> request) {

        PushNotificationRequest notification = PushNotificationRequest.builder()
                .title("🧪 Test Notifica")
                .body("Questa è una notifica di test da Spring Weather Broadcast!")
                .icon("/icons/icon-192x192.png")
                .clickUrl("/")
                .tag("test-notification")
                .priority("high")
                .build();

        // Se fornito un token, invia al dispositivo, altrimenti broadcast
        if (request != null && request.containsKey("token")) {
            notification.setToken(request.get("token"));
            return sendToDevice(notification);
        } else {
            return sendBroadcast(notification);
        }
    }
}
