package ch.loete.backend.domain.exception;

import ch.loete.backend.web.dto.response.ErrorResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Globaler Exception-Handler für die gesamte REST-API.
 *
 * <p>Fängt alle Exceptions ab und wandelt sie in einheitliche {@link ErrorResponse}-Objekte mit
 * passenden HTTP-Statuscodes um.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Behandelt ResourceNotFoundException und gibt HTTP 404 zurück.
   *
   * @param ex die geworfene Exception
   * @return die Fehlerantwort mit Status 404
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, ex.getMessage(), Instant.now()));
  }

  /**
   * Behandelt DuplicateResourceException und gibt HTTP 409 zurück.
   *
   * @param ex die geworfene Exception
   * @return die Fehlerantwort mit Status 409
   */
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(409, ex.getMessage(), Instant.now()));
  }

  /**
   * Behandelt AuthenticationException und gibt HTTP 401 zurück.
   *
   * @param ex die geworfene Exception
   * @return die Fehlerantwort mit Status 401
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(401, ex.getMessage(), Instant.now()));
  }

  /**
   * Behandelt BadCredentialsException (Spring Security) und gibt HTTP 401 zurück.
   *
   * @param ex die geworfene Exception
   * @return die Fehlerantwort mit Status 401
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(401, "Invalid email or password", Instant.now()));
  }

  /**
   * Behandelt ResponseStatusException und gibt den entsprechenden HTTP-Status zurück.
   *
   * @param ex die geworfene Exception
   * @return die Fehlerantwort mit dem jeweiligen Status
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    return ResponseEntity.status(status)
        .body(
            new ErrorResponse(
                status.value(),
                ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(),
                Instant.now()));
  }

  /**
   * Behandelt Validierungsfehler bei Request-Bodies und gibt HTTP 400 zurück.
   *
   * @param ex die geworfene Exception
   * @return die Fehlerantwort mit Status 400 und allen Feldfehlern
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
    return ResponseEntity.badRequest().body(new ErrorResponse(400, message, Instant.now()));
  }

  /**
   * Behandelt einen Request mit unerlaubter HTTP-Methode und gibt HTTP 405 zurück.
   *
   * @param ex die geworfene Exception
   * @return die Fehlerantwort mit Status 405
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new ErrorResponse(405, ex.getMessage(), Instant.now()));
  }

  /**
   * Fängt alle nicht anderweitig behandelten Exceptions ab und gibt HTTP 500 zurück.
   *
   * @param ex die geworfene Exception
   * @return die Fehlerantwort mit Status 500
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, "Internal server error", Instant.now()));
  }
}
