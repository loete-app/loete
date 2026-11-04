package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  @Modifying
  void deleteByToken(String token);

  @Modifying
  void deleteByUserId(String userId);

  @Modifying
  void deleteByExpiresAtBefore(Instant now);
}
