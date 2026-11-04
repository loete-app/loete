package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request-DTO für die Benutzer-Anmeldung.
 *
 * @param email die E-Mail-Adresse des Benutzers
 * @param password das Passwort des Benutzers
 */
public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
