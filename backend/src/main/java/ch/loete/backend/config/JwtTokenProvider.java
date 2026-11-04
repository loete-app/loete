package ch.loete.backend.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final SecretKey key;
  private final long accessExpirationMs;

  public JwtTokenProvider(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessExpirationMs = accessExpirationMs;
  }

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

  public String getUserIdFromToken(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
  }

  public String getEmailFromToken(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("email", String.class);
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
