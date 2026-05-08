package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

/** JPA-Repository für den Datenbankzugriff auf {@link RefreshToken}-Entitäten. */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * Findet einen Refresh-Token anhand seines Token-Werts.
   *
   * @param token der Token-Wert
   * @return das RefreshToken, falls vorhanden
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * Löscht einen Refresh-Token anhand seines Token-Werts.
   *
   * @param token der Token-Wert
   */
  @Modifying
  void deleteByToken(String token);

  /**
   * Löscht alle Refresh-Tokens eines Benutzers.
   *
   * @param userId die Benutzer-ID
   */
  @Modifying
  void deleteByUserId(String userId);

  /**
   * Löscht alle abgelaufenen Refresh-Tokens.
   *
   * @param now der aktuelle Zeitpunkt als Vergleichswert
   */
  @Modifying
  void deleteByExpiresAtBefore(Instant now);
}
