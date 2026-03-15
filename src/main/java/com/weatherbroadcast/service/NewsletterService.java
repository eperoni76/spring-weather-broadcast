package com.weatherbroadcast.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.weatherbroadcast.dto.NewsletterRecipientScope;
import com.weatherbroadcast.dto.NewsletterRequest;
import com.weatherbroadcast.dto.NewsletterResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Servizio per l'invio di newsletter HTML ai destinatari presenti in Firestore.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterService {

    private static final String USERS_COLLECTION = "utenti";

    private final JavaMailSender mailSender;

    @Value("${newsletter.mail-from:${spring.mail.username:}}")
    private String configuredFromAddress;

    public NewsletterResponse sendNewsletter(NewsletterRequest request, String requestedBy) {
        validateRequest(request);

        RecipientResolution recipients = resolveRecipients(request.getRecipientScope());
        if (recipients.emails().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nessun destinatario valido trovato per il pubblico selezionato");
        }

        String fromAddress = resolveFromAddress();
        String htmlContent = buildHtmlContent(request);
        String plainTextContent = buildPlainTextContent(request);

        int sentCount = 0;
        int failedCount = recipients.invalidEmailsCount();

        for (String recipient : recipients.emails()) {
            try {
                sendEmail(recipient, fromAddress, request.getSubject().trim(), plainTextContent, htmlContent);
                sentCount++;
            } catch (MessagingException | MailException exception) {
                failedCount++;
                log.error("Errore nell'invio della newsletter a {}", recipient, exception);
            }
        }

        if (sentCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Invio newsletter fallito per tutti i destinatari");
        }

        String responseMessage = failedCount == 0
                ? "Newsletter inviata con successo a %d destinatari".formatted(sentCount)
                : "Newsletter inviata a %d destinatari, %d non consegnati o scartati".formatted(sentCount,
                        failedCount);

        log.info("Newsletter inviata da {} con scope {}: {} invii riusciti, {} non consegnati",
                requestedBy, request.getRecipientScope(), sentCount, failedCount);

        return NewsletterResponse.success(
                responseMessage,
                request.getRecipientScope(),
                sentCount,
                failedCount);
    }

    private void validateRequest(NewsletterRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload newsletter mancante");
        }

        if (isBlank(request.getSubject())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Oggetto email obbligatorio");
        }

        if (isBlank(request.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titolo newsletter obbligatorio");
        }

        if (isBlank(request.getBody())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contenuto newsletter obbligatorio");
        }

        if (request.getRecipientScope() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seleziona il pubblico destinatario della newsletter");
        }

        boolean hasCtaLabel = !isBlank(request.getCtaLabel());
        boolean hasCtaUrl = !isBlank(request.getCtaUrl());
        if (hasCtaLabel != hasCtaUrl) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "CTA incompleta: etichetta e URL devono essere valorizzati insieme");
        }

        if (hasCtaUrl && !isValidAbsoluteUrl(request.getCtaUrl())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'URL della CTA deve essere un indirizzo assoluto http o https");
        }
    }

    private RecipientResolution resolveRecipients(NewsletterRecipientScope scope) {
        try {
            QuerySnapshot snapshot = switch (scope) {
                case ADMINS -> FirestoreClient.getFirestore()
                        .collection(USERS_COLLECTION)
                        .whereEqualTo("ruolo", "admin")
                        .get()
                        .get();
                case ALL_USERS -> FirestoreClient.getFirestore()
                        .collection(USERS_COLLECTION)
                        .get()
                        .get();
            };

            LinkedHashSet<String> emails = new LinkedHashSet<>();
            int invalidEmailsCount = 0;

            for (DocumentSnapshot document : snapshot.getDocuments()) {
                String email = trimToNull(document.getString("email"));
                if (email == null || !isValidEmail(email)) {
                    invalidEmailsCount++;
                    continue;
                }

                emails.add(email.toLowerCase(Locale.ROOT));
            }

            return new RecipientResolution(new ArrayList<>(emails), invalidEmailsCount);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Recupero destinatari newsletter interrotto", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nel recupero dei destinatari", exception);
        } catch (ExecutionException exception) {
            log.error("Errore nel recupero destinatari newsletter da Firestore", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nel recupero dei destinatari", exception);
        }
    }

    private void sendEmail(
            String recipient,
            String fromAddress,
            String subject,
            String plainTextContent,
            String htmlContent) throws MessagingException {

        var mimeMessage = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(plainTextContent, htmlContent);

        mailSender.send(mimeMessage);
    }

    private String resolveFromAddress() {
        String fromAddress = trimToNull(configuredFromAddress);
        if (fromAddress == null || !isValidEmail(fromAddress)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Configurazione SMTP incompleta: newsletter.mail-from o spring.mail.username non valido");
        }

        return fromAddress;
    }

    private String buildHtmlContent(NewsletterRequest request) {
        String introSection = isBlank(request.getIntro())
                ? ""
                : "<p style=\"margin:0 0 18px;color:#4a5568;font-size:16px;line-height:1.7;\">"
                        + toHtmlParagraph(request.getIntro())
                        + "</p>";

        String ctaSection = isBlank(request.getCtaLabel()) || isBlank(request.getCtaUrl())
                ? ""
                : "<div style=\"margin:28px 0 24px;\">"
                        + "<a href=\"" + HtmlUtils.htmlEscape(request.getCtaUrl().trim())
                        + "\" style=\"display:inline-block;padding:14px 24px;border-radius:999px;"
                        + "background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:#ffffff;"
                        + "font-weight:700;text-decoration:none;\">"
                        + HtmlUtils.htmlEscape(request.getCtaLabel().trim())
                        + "</a></div>";

        String footerSection = isBlank(request.getFooterNote())
                ? ""
                : "<div style=\"margin-top:28px;padding-top:20px;border-top:1px solid #e2e8f0;\">"
                        + "<p style=\"margin:0;color:#718096;font-size:13px;line-height:1.6;\">"
                        + toHtmlParagraph(request.getFooterNote())
                        + "</p></div>";

        return """
                <!DOCTYPE html>
                <html lang="it">
                <body style="margin:0;padding:32px 16px;background:#f5f7fa;font-family:Arial,'Helvetica Neue',sans-serif;color:#2d3748;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:680px;margin:0 auto;">
                    <tr>
                      <td style="padding:0;">
                        <div style="padding:28px 32px;border-radius:28px;background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);color:#ffffff;box-shadow:0 18px 40px rgba(102,126,234,0.24);">
                          <div style="font-size:13px;letter-spacing:0.12em;text-transform:uppercase;opacity:0.82;">Newsletter Il Crima</div>
                          <h1 style="margin:12px 0 0;font-size:32px;line-height:1.15;">%s</h1>
                        </div>
                        <div style="margin-top:20px;background:#ffffff;border-radius:24px;padding:32px;box-shadow:0 12px 30px rgba(15,23,42,0.08);">
                          %s
                          <div style="color:#2d3748;font-size:16px;line-height:1.75;white-space:normal;">%s</div>
                          %s
                          %s
                        </div>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(request.getTitle().trim()),
                introSection,
                toHtmlParagraph(request.getBody()),
                ctaSection,
                footerSection);
    }

    private String buildPlainTextContent(NewsletterRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(cleanText(request.getTitle())).append("\n\n");

        if (!isBlank(request.getIntro())) {
            builder.append(cleanText(request.getIntro())).append("\n\n");
        }

        builder.append(cleanText(request.getBody()));

        if (!isBlank(request.getCtaLabel()) && !isBlank(request.getCtaUrl())) {
            builder.append("\n\n")
                    .append(cleanText(request.getCtaLabel()))
                    .append(": ")
                    .append(request.getCtaUrl().trim());
        }

        if (!isBlank(request.getFooterNote())) {
            builder.append("\n\n")
                    .append(cleanText(request.getFooterNote()));
        }

        return builder.toString();
    }

    private String toHtmlParagraph(String value) {
        return HtmlUtils.htmlEscape(cleanText(value))
                .replace("\n", "<br/>");
    }

    private String cleanText(String value) {
        return trimToNull(value) == null ? "" : value.trim().replace("\r\n", "\n").replace('\r', '\n');
    }

    private boolean isValidEmail(String email) {
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            return true;
        } catch (MessagingException exception) {
            return false;
        }
    }

    private boolean isValidAbsoluteUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            return uri.isAbsolute() && scheme != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record RecipientResolution(List<String> emails, int invalidEmailsCount) {
    }
}