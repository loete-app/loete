package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Event;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EventRepository
    extends JpaRepository<Event, String>, JpaSpecificationExecutor<Event> {

  Optional<Event> findByExternalId(String externalId);

  @Modifying
  @Transactional
  int deleteByStartDateBefore(LocalDateTime cutoff);
}
