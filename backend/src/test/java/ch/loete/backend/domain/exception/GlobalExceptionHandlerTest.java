package ch.loete.backend.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import ch.loete.backend.web.dto.response.ErrorResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @InjectMocks private GlobalExceptionHandler handler;

  @Test
  void handleNotFound_returns404WithMessage() {
    ResourceNotFoundException ex = new ResourceNotFoundException("Event", "abc123");

    ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(404);
    assertThat(response.getBody().message()).isEqualTo("Event with id 'abc123' not found");
    assertThat(response.getBody().timestamp()).isNotNull();
  }

  @Test
  void handleDuplicate_returns409WithMessage() {
    DuplicateResourceException ex = new DuplicateResourceException("Favorite", "eventId", "evt001");

    ResponseEntity<ErrorResponse> response = handler.handleDuplicate(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(409);
    assertThat(response.getBody().message())
        .isEqualTo("Favorite with eventId 'evt001' already exists");
    assertThat(response.getBody().timestamp()).isNotNull();
  }

  @Test
  void handleAuthentication_returns401WithMessage() {
    AuthenticationException ex = new AuthenticationException("Invalid credentials");

    ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(401);
    assertThat(response.getBody().message()).isEqualTo("Invalid credentials");
    assertThat(response.getBody().timestamp()).isNotNull();
  }

  @Test
  void handleBadCredentials_returns401WithMessage() {
    BadCredentialsException ex = new BadCredentialsException("Bad credentials");

    ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(401);
    assertThat(response.getBody().message()).isEqualTo("Invalid email or password");
    assertThat(response.getBody().timestamp()).isNotNull();
  }

  @Test
  void handleResponseStatus_returnsCorrectStatus() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

    ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(403);
    assertThat(response.getBody().message()).isEqualTo("Access denied");
    assertThat(response.getBody().timestamp()).isNotNull();
  }

  @Test
  void handleResponseStatus_usesReasonPhraseWhenReasonIsNull() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);

    ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(400);
    assertThat(response.getBody().message()).isEqualTo("Bad Request");
  }

  @Test
  void handleValidation_returns400WithFieldErrors() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError1 = new FieldError("request", "title", "must not be blank");
    FieldError fieldError2 = new FieldError("request", "price", "must be positive");

    given(bindingResult.getFieldErrors()).willReturn(List.of(fieldError1, fieldError2));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(400);
    assertThat(response.getBody().message()).contains("title: must not be blank");
    assertThat(response.getBody().message()).contains("price: must be positive");
    assertThat(response.getBody().timestamp()).isNotNull();
  }

  @Test
  void handleGeneric_returns500() {
    Exception ex = new RuntimeException("Something went wrong");

    ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(500);
    assertThat(response.getBody().message()).isEqualTo("Internal server error");
    assertThat(response.getBody().timestamp()).isNotNull();
  }
}
