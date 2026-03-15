package com.weatherbroadcast.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Verifica che la richiesta REST provenga da un utente admin autenticato via Firebase.
 */
@Slf4j
@Service
public class AdminAuthorizationService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USERS_COLLECTION = "utenti";

    public AdminIdentity verifyAdmin(String authorizationHeader) {
        String idToken = extractBearerToken(authorizationHeader);

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            AdminIdentity identity = loadAdminIdentity(decodedToken);

            log.info("Richiesta admin autorizzata per utente {}", identity.email());
            return identity;
        } catch (FirebaseAuthException exception) {
            log.warn("Token Firebase non valido durante la verifica admin: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Firebase non valido", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Verifica admin interrotta", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore interno durante la verifica admin", exception);
        } catch (ExecutionException exception) {
            log.error("Errore nel recupero del profilo admin da Firestore", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nel recupero del profilo admin", exception);
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Header Authorization Bearer mancante o non valido");
        }

        String idToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (idToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token Firebase mancante");
        }

        return idToken;
    }

    @SuppressWarnings("null")
    private AdminIdentity loadAdminIdentity(FirebaseToken decodedToken)
            throws ExecutionException, InterruptedException {

        String uid = decodedToken.getUid();
        if (uid == null || uid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Token Firebase privo di UID valido");
        }

        String normalizedUid = Objects.requireNonNull(uid).trim();
        DocumentSnapshot userDocument = FirestoreClient.getFirestore()
                .collection(USERS_COLLECTION)
            .document(normalizedUid)
                .get()
                .get();

        if (!userDocument.exists()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Profilo utente non trovato in Firestore");
        }

        String role = userDocument.getString("ruolo");
        if (role == null || !"admin".equalsIgnoreCase(role.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Utente non autorizzato all'invio della newsletter");
        }

        String email = decodedToken.getEmail();
        if (email == null || email.isBlank()) {
            email = userDocument.getString("email");
        }

        return new AdminIdentity(normalizedUid, email != null && !email.isBlank() ? email : normalizedUid);
    }

    public record AdminIdentity(String uid, String email) {
    }
}