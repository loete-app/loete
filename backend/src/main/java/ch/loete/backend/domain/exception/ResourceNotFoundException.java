package ch.loete.backend.domain.exception;

/**
 * Exception für nicht gefundene Ressourcen.
 *
 * <p>Wird geworfen, wenn eine angeforderte Ressource (z.B. Event, User) nicht in der Datenbank
 * gefunden wird. Resultiert in einem HTTP-404-Response.
 */
public class ResourceNotFoundException extends RuntimeException {

  /**
   * Erstellt eine neue ResourceNotFoundException.
   *
   * @param resource der Ressourcentyp (z.B. "Event", "User")
   * @param id die gesuchte ID
   */
  public ResourceNotFoundException(String resource, String id) {
    super(resource + " with id '" + id + "' not found");
  }
}
