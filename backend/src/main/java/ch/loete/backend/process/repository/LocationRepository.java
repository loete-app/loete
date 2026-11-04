package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Location;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** JPA-Repository für den Datenbankzugriff auf {@link Location}-Entitäten. */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

  /**
   * Findet eine Location anhand von Name und Stadt.
   *
   * @param name der Name des Veranstaltungsortes
   * @param city die Stadt
   * @return die Location, falls vorhanden
   */
  Optional<Location> findByNameAndCity(String name, String city);

  /**
   * Gibt alle Städte zurück, in denen mindestens ein Event stattfindet.
   *
   * @return alphabetisch sortierte Liste der Staedtenamen
   */
  @Query(
      "SELECT DISTINCT l.city FROM Location l"
          + " WHERE l.city IS NOT NULL"
          + " AND EXISTS (SELECT 1 FROM Event e WHERE e.location = l)"
          + " ORDER BY l.city ASC")
  List<String> findDistinctCitiesWithEvents();
}
