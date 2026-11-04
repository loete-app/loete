package ch.loete.backend.web.dto.response;

import java.time.Instant;

/**
 * Response-DTO für Fehlerantworten der REST-API.
 *
 * <p>Wird vom {@link ch.loete.backend.domain.exception.GlobalExceptionHandler} für alle
 * Fehlerfaelle als einheitliches Antwortformat verwendet.
 *
 * @param status der HTTP-Statuscode
 * @param message die Fehlermeldung
 * @param timestamp der Zeitpunkt des Fehlers
 */
public record ErrorResponse(int status, String message, Instant timestamp) {}
