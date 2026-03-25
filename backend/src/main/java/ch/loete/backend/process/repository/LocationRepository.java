package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Location;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByNameAndCity(String name, String city);
}
