package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.AuthService;
import ch.loete.backend.web.dto.request.LoginRequest;
import ch.loete.backend.web.dto.request.RefreshRequest;
import ch.loete.backend.web.dto.request.RegisterRequest;
import ch.loete.backend.web.dto.response.AuthResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für Authentifizierungsendpunkte.
 *
 * <p>Stellt Endpunkte für Registrierung, Login, Token-Refresh und Logout bereit. Alle Endpunkte
 * sind öffentlich zugaenglich (kein JWT erforderlich).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  /** Service für Authentifizierungsoperationen. */
  private final AuthService authService;

  /**
   * Registriert einen neuen Benutzer.
   *
   * @param request die Registrierungsdaten
   * @return die Authentifizierungsantwort mit Token-Paar (HTTP 201)
   */
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  /**
   * Meldet einen Benutzer an.
   *
   * @param request die Login-Daten (E-Mail, Passwort)
   * @return die Authentifizierungsantwort mit Token-Paar
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  /**
   * Erneuert ein Token-Paar anhand eines Refresh-Tokens.
   *
   * @param request der Refresh-Request mit dem aktuellen Refresh-Token
   * @return die Authentifizierungsantwort mit neuem Token-Paar
   */
  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request));
  }

  /**
   * Meldet einen Benutzer ab und invalidiert das Refresh-Token.
   *
   * @param request der Request mit dem zu invalidierenden Refresh-Token
   * @return Erfolgsmeldung
   */
  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(@RequestBody RefreshRequest request) {
    authService.logout(request.refreshToken());
    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
  }
}
