package ch.loete.backend.domain.exception;

/**
 * Exception für doppelte Ressourcen (Konflikte).
 *
 * <p>Wird geworfen, wenn versucht wird, eine Ressource mit einem bereits existierenden eindeutigen
 * Wert zu erstellen (z.B. doppelte E-Mail bei Registrierung). Resultiert in einem
 * HTTP-409-Response.
 */
public class DuplicateResourceException extends RuntimeException {

  /**
   * Erstellt eine neue DuplicateResourceException mit einer Fehlermeldung.
   *
   * @param message die Fehlermeldung
   */
  public DuplicateResourceException(String message) {
    super(message);
  }

  /**
   * Erstellt eine neue DuplicateResourceException mit Ressource, Feld und Wert.
   *
   * @param resource der Ressourcentyp (z.B. "User")
   * @param field das doppelte Feld (z.B. "email")
   * @param value der doppelte Wert
   */
  public DuplicateResourceException(String resource, String field, String value) {
    super(resource + " with " + field + " '" + value + "' already exists");
  }
}
