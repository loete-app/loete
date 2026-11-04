package ch.loete.backend.web.dto.response;

/**
 * Response-DTO für Authentifizierungsantworten.
 *
 * <p>Wird bei Registrierung, Login und Token-Refresh zurückgegeben und enthält das
 * JWT-Access-Token, das Refresh-Token sowie die grundlegenden Benutzerdaten.
 *
 * @param accessToken das JWT-Access-Token
 * @param refreshToken das Refresh-Token (UUID)
 * @param userId die Benutzer-ID
 * @param username der Benutzername
 * @param email die E-Mail-Adresse
 */
public record AuthResponse(
    String accessToken, String refreshToken, String userId, String username, String email) {}
