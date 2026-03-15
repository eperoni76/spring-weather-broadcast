package com.weatherbroadcast.controller;

import com.weatherbroadcast.dto.NewsletterRequest;
import com.weatherbroadcast.dto.NewsletterResponse;
import com.weatherbroadcast.service.AdminAuthorizationService;
import com.weatherbroadcast.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST per l'invio di newsletter da parte degli admin.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;
    private final AdminAuthorizationService adminAuthorizationService;

    @PostMapping("/send")
    public ResponseEntity<NewsletterResponse> sendNewsletter(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestBody NewsletterRequest request) {

        AdminAuthorizationService.AdminIdentity adminIdentity =
                adminAuthorizationService.verifyAdmin(authorizationHeader);

        log.info("Ricevuta richiesta newsletter da admin {}", adminIdentity.email());
        NewsletterResponse response = newsletterService.sendNewsletter(request, adminIdentity.email());

        HttpStatus status = response.getFailedRecipientCount() != null && response.getFailedRecipientCount() > 0
                ? HttpStatus.ACCEPTED
                : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }
}