package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Location;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

  Optional<Location> findByNameAndCity(String name, String city);

  @Query(
      "SELECT DISTINCT l.city FROM Location l"
          + " WHERE l.city IS NOT NULL"
          + " AND EXISTS (SELECT 1 FROM Event e WHERE e.location = l)"
          + " ORDER BY l.city ASC")
  List<String> findDistinctCitiesWithEvents();
}
