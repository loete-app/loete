package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {

  List<Favorite> findByClientIdOrderByCreatedAtDesc(String clientId);

  Optional<Favorite> findByClientIdAndEventId(String clientId, String eventId);

  boolean existsByClientIdAndEventId(String clientId, String eventId);

  @Query("SELECT f.event.id FROM Favorite f WHERE f.clientId = :clientId")
  Set<String> findEventIdsByClientId(@Param("clientId") String clientId);
}
