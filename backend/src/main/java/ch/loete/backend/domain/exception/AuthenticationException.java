package ch.loete.backend.domain.exception;

/**
 * Exception für Authentifizierungsfehler.
 *
 * <p>Wird geworfen bei ungültigem Login, abgelaufenem Refresh-Token oder anderen
 * authentifizierungsbezogenen Fehlern. Resultiert in einem HTTP-401-Response.
 */
public class AuthenticationException extends RuntimeException {

  /**
   * Erstellt eine neue AuthenticationException mit der angegebenen Fehlermeldung.
   *
   * @param message die Fehlermeldung
   */
  public AuthenticationException(String message) {
    super(message);
  }
}
