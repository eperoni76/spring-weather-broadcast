package com.weatherbroadcast.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Configurazione Firebase Admin SDK per Spring Boot
 * 
 * Inizializza Firebase all'avvio dell'applicazione.
 * Supporta sia file locale (sviluppo) che variabile d'ambiente base64
 * (produzione).
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path:}")
    private Resource serviceAccountResource;

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @Value("${firebase.database-url}")
    private String databaseUrl;

    /**
     * Inizializza Firebase Admin SDK
     * 
     * Supporta due modalità:
     * 1. Produzione: credenziali da variabile d'ambiente (base64 encoded JSON)
     * 2. Sviluppo: credenziali da file classpath
     * 
     * @return FirebaseApp instance
     * @throws IOException se le credenziali non sono trovate o non valide
     */
    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("Inizializzazione Firebase Admin SDK...");

            GoogleCredentials credentials;

            // Produzione: credenziali da variabile d'ambiente (base64)
            if (serviceAccountJson != null && !serviceAccountJson.isEmpty()) {
                log.info("Caricamento credenziali Firebase da variabile d'ambiente");
                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(serviceAccountJson);
                    credentials = GoogleCredentials.fromStream(
                            new ByteArrayInputStream(decodedBytes));
                } catch (IllegalArgumentException e) {
                    log.error("Errore nel decodificare le credenziali base64", e);
                    throw new IOException("Credenziali Firebase non valide", e);
                }
            }
            // Sviluppo: credenziali da file
            else if (serviceAccountResource != null && serviceAccountResource.exists()) {
                log.info("Caricamento credenziali Firebase da file");
                credentials = GoogleCredentials.fromStream(
                        serviceAccountResource.getInputStream());
            } else {
                throw new IOException(
                        "Credenziali Firebase non trovate. " +
                                "Configura 'firebase.service-account-json' (produzione) " +
                                "o 'firebase.service-account-path' (sviluppo)");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl(databaseUrl)
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK inizializzato con successo!");
            return app;
        } else {
            log.info("Firebase Admin SDK già inizializzato");
            return FirebaseApp.getInstance();
        }
    }
}
