package ch.loete.backend.domain.service;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.exception.AuthenticationException;
import ch.loete.backend.domain.exception.DuplicateResourceException;
import ch.loete.backend.domain.model.RefreshToken;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.process.repository.RefreshTokenRepository;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.request.LoginRequest;
import ch.loete.backend.web.dto.request.RefreshRequest;
import ch.loete.backend.web.dto.request.RegisterRequest;
import ch.loete.backend.web.dto.response.AuthResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service für Authentifizierungs- und Autorisierungsoperationen.
 *
 * <p>Verwaltet Registrierung, Login, Token-Refresh und Logout. Erzeugt JWT-Access-Tokens und
 * UUID-basierte Refresh-Tokens mit Token-Rotation (altes Token wird bei Refresh gelöscht).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  /** Repository für den Zugriff auf Benutzer-Daten. */
  private final UserRepository userRepository;

  /** Repository für den Zugriff auf Refresh-Token-Daten. */
  private final RefreshTokenRepository refreshTokenRepository;

  /** Encoder für die Passwort-Hashung mit BCrypt. */
  private final PasswordEncoder passwordEncoder;

  /** Provider für die JWT-Token-Generierung. */
  private final JwtTokenProvider jwtTokenProvider;

  /** Manager für die Spring-Security-Authentifizierung. */
  private final AuthenticationManager authenticationManager;

  /** Gültigkeitsdauer eines Refresh-Tokens in Millisekunden. */
  @Value("${app.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  /**
   * Registriert einen neuen Benutzer und gibt ein Token-Paar zurück.
   *
   * @param request die Registrierungsdaten (E-Mail, Benutzername, Passwort)
   * @return die Authentifizierungsantwort mit Access- und Refresh-Token
   * @throws DuplicateResourceException wenn E-Mail oder Benutzername bereits existieren
   */
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())
        || userRepository.existsByUsername(request.username())) {
      throw new DuplicateResourceException("An account with this email or username already exists");
    }

    User user =
        User.builder()
            .email(request.email())
            .username(request.username())
            .passwordHash(passwordEncoder.encode(request.password()))
            .build();
    user = userRepository.save(user);

    return buildAuthResponse(user);
  }

  /**
   * Authentifiziert einen Benutzer und gibt ein Token-Paar zurück.
   *
   * @param request die Login-Daten (E-Mail, Passwort)
   * @return die Authentifizierungsantwort mit Access- und Refresh-Token
   * @throws AuthenticationException wenn die Anmeldedaten ungültig sind
   */
  @Transactional
  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

    return buildAuthResponse(user);
  }

  /**
   * Erneuert ein Token-Paar anhand eines gültigen Refresh-Tokens.
   *
   * <p>Das alte Refresh-Token wird gelöscht und ein neues generiert (Token-Rotation).
   *
   * @param request der Refresh-Request mit dem aktuellen Refresh-Token
   * @return die Authentifizierungsantwort mit neuem Access- und Refresh-Token
   * @throws AuthenticationException wenn das Refresh-Token ungültig oder abgelaufen ist
   */
  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByToken(request.refreshToken())
            .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

    if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
      refreshTokenRepository.delete(refreshToken);
      throw new AuthenticationException("Refresh token expired");
    }

    User user = refreshToken.getUser();
    refreshTokenRepository.delete(refreshToken);

    return buildAuthResponse(user);
  }

  /**
   * Meldet einen Benutzer ab, indem das Refresh-Token gelöscht wird.
   *
   * @param refreshToken das zu loeschende Refresh-Token
   */
  @Transactional
  public void logout(String refreshToken) {
    refreshTokenRepository.deleteByToken(refreshToken);
  }

  /**
   * Erstellt eine AuthResponse mit neuem Access- und Refresh-Token für den Benutzer.
   *
   * @param user der Benutzer, für den die Tokens generiert werden
   * @return die vollständige Authentifizierungsantwort
   */
  private AuthResponse buildAuthResponse(User user) {
    String accessToken =
        jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getEmail());

    RefreshToken refreshToken =
        RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
            .build();
    refreshToken = refreshTokenRepository.save(refreshToken);

    return new AuthResponse(
        accessToken, refreshToken.getToken(), user.getId(), user.getUsername(), user.getEmail());
  }
}
