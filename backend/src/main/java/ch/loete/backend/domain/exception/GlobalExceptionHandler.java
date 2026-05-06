package ch.loete.backend.domain.exception;

import ch.loete.backend.web.dto.response.ErrorResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, ex.getMessage(), Instant.now()));
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(409, ex.getMessage(), Instant.now()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(401, ex.getMessage(), Instant.now()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(401, "Invalid email or password", Instant.now()));
  }

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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
    return ResponseEntity.badRequest().body(new ErrorResponse(400, message, Instant.now()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, "Internal server error", Instant.now()));
  }
}
