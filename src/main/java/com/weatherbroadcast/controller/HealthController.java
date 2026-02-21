package com.weatherbroadcast.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller per health check e status dell'applicazione
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class HealthController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Health check endpoint
     *
     * GET /api/health
     *
     * @return Stato dell'applicazione con timestamp
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("Health check richiesto");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(FORMATTER));
        health.put("service", "spring-weather-broadcast");
        health.put("version", "0.0.1-SNAPSHOT");

        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint per informazioni sul servizio
     *
     * GET /api/info
     *
     * @return Informazioni sul servizio
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        log.debug("Info richiesto");

        Map<String, Object> info = new HashMap<>();
        info.put("name", "Spring Weather Broadcast");
        info.put("description", "Backend per notifiche push Weather Broadcast");
        info.put("version", "0.0.1-SNAPSHOT");
        info.put("java", System.getProperty("java.version"));
        info.put("spring", org.springframework.boot.SpringBootVersion.getVersion());

        return ResponseEntity.ok(info);
    }
}
