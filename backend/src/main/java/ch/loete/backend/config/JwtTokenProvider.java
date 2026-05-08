package ch.loete.backend.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provider für JWT-Token-Operationen.
 *
 * <p>Verwaltet die Erstellung, Validierung und das Parsen von JSON Web Tokens (JWT) für die
 * Benutzer-Authentifizierung. Verwendet HMAC-SHA512 als Signaturalgorithmus.
 */
@Component
public class JwtTokenProvider {

  /** Geheimer Schlüssel für die HMAC-SHA512-Signierung der Tokens. */
  private final SecretKey key;

  /** Gültigkeitsdauer eines Access-Tokens in Millisekunden. */
  private final long accessExpirationMs;

  /**
   * Erstellt einen neuen JwtTokenProvider.
   *
   * @param secret der geheime Schlüssel für die Token-Signierung
   * @param accessExpirationMs die Gültigkeitsdauer eines Access-Tokens in Millisekunden
   */
  public JwtTokenProvider(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessExpirationMs = accessExpirationMs;
  }

  /**
   * Generiert ein neues JWT-Access-Token für den angegebenen Benutzer.
   *
   * @param userId die eindeutige Benutzer-ID (wird als Subject gesetzt)
   * @param username der Benutzername (als Claim)
   * @param email die E-Mail-Adresse (als Claim)
   * @return das signierte JWT-Token als String
   */
  public String generateToken(String userId, String username, String email) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessExpirationMs);

    return Jwts.builder()
        .subject(userId)
        .claim("username", username)
        .claim("email", email)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key, Jwts.SIG.HS512)
        .compact();
  }

  /**
   * Extrahiert die Benutzer-ID (Subject) aus einem JWT-Token.
   *
   * @param token das zu parsende JWT-Token
   * @return die Benutzer-ID
   */
  public String getUserIdFromToken(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
  }

  /**
   * Extrahiert die E-Mail-Adresse aus einem JWT-Token.
   *
   * @param token das zu parsende JWT-Token
   * @return die E-Mail-Adresse
   */
  public String getEmailFromToken(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("email", String.class);
  }

  /**
   * Validiert ein JWT-Token auf Gültigkeit (Signatur und Ablaufdatum).
   *
   * @param token das zu validierende JWT-Token
   * @return {@code true} wenn das Token gültig ist, {@code false} sonst
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
