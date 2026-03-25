package ch.loete.backend.domain.exception;

public class DuplicateResourceException extends RuntimeException {

  public DuplicateResourceException(String resource, String field, String value) {
    super(resource + " with " + field + " '" + value + "' already exists");
  }
}
