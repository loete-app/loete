package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request-DTO für die Benutzer-Registrierung.
 *
 * @param email die E-Mail-Adresse (muss gültig sein)
 * @param username der Benutzername (3-50 Zeichen)
 * @param password das Passwort (mindestens 6 Zeichen)
 */
public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Size(min = 6) String password) {}
