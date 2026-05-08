package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA-Repository für den Datenbankzugriff auf {@link Favorite}-Entitäten. */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {

  /**
   * Findet alle Favoriten eines Benutzers, sortiert nach Erstellungsdatum (neueste zuerst).
   *
   * @param userId die Benutzer-ID
   * @return die Favoriten-Liste
   */
  List<Favorite> findByUser_IdOrderByCreatedAtDesc(String userId);

  /**
   * Findet einen Favoriten anhand von Benutzer-ID und Event-ID.
   *
   * @param userId die Benutzer-ID
   * @param eventId die Event-ID
   * @return der Favorit, falls vorhanden
   */
  Optional<Favorite> findByUser_IdAndEvent_Id(String userId, String eventId);

  /**
   * Prüft, ob ein Favorit für den gegebenen Benutzer und das Event existiert.
   *
   * @param userId die Benutzer-ID
   * @param eventId die Event-ID
   * @return {@code true} wenn der Favorit existiert
   */
  boolean existsByUser_IdAndEvent_Id(String userId, String eventId);

  /**
   * Löscht einen Favoriten anhand von Benutzer-ID und Event-ID.
   *
   * @param userId die Benutzer-ID
   * @param eventId die Event-ID
   */
  void deleteByUser_IdAndEvent_Id(String userId, String eventId);

  /**
   * Gibt die Event-IDs aller Favoriten eines Benutzers zurück.
   *
   * @param userId die Benutzer-ID
   * @return Set der favorisierten Event-IDs
   */
  @Query("SELECT f.event.id FROM Favorite f WHERE f.user.id = :userId")
  Set<String> findEventIdsByUserId(@Param("userId") String userId);
}
