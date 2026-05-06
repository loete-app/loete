package ch.loete.backend.process.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.testconfig.TestcontainersConfig;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class EventRepositoryTest {

  @Autowired private EventRepository eventRepository;

  @Autowired private TestEntityManager entityManager;

  private Category category;
  private Location location;

  @BeforeEach
  void setUp() {
    // Use a pre-seeded category from the V1 migration
    category =
        entityManager
            .getEntityManager()
            .createQuery("SELECT c FROM Category c WHERE c.slug = 'konzert'", Category.class)
            .getSingleResult();

    location =
        Location.builder()
            .name("Hallenstadion")
            .city("Zurich")
            .country("CH")
            .latitude(47.41)
            .longitude(8.55)
            .build();
    location = entityManager.persist(location);
    entityManager.flush();
  }

  @Test
  void findByExternalId_returnsMatchingEvent() {
    Event event =
        Event.builder()
            .externalId("TM-12345")
            .name("Test Concert")
            .startDate(LocalDateTime.of(2026, 8, 15, 20, 0))
            .category(category)
            .location(location)
            .build();
    entityManager.persist(event);
    entityManager.flush();

    Optional<Event> result = eventRepository.findByExternalId("TM-12345");

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Test Concert");
    assertThat(result.get().getId()).isNotNull().hasSize(8);
  }

  @Test
  void findByExternalId_returnsEmptyWhenNotFound() {
    Optional<Event> result = eventRepository.findByExternalId("NONEXISTENT");

    assertThat(result).isEmpty();
  }

  @Test
  void deleteByStartDateBefore_deletesOldEvents() {
    Event oldEvent =
        Event.builder()
            .externalId("OLD-001")
            .name("Old Concert")
            .startDate(LocalDateTime.of(2024, 1, 1, 20, 0))
            .category(category)
            .location(location)
            .build();
    entityManager.persist(oldEvent);

    Event futureEvent =
        Event.builder()
            .externalId("FUT-001")
            .name("Future Concert")
            .startDate(LocalDateTime.of(2027, 6, 15, 20, 0))
            .category(category)
            .location(location)
            .build();
    entityManager.persist(futureEvent);
    entityManager.flush();

    int deleted = eventRepository.deleteByStartDateBefore(LocalDateTime.of(2025, 1, 1, 0, 0));

    assertThat(deleted).isEqualTo(1);
    assertThat(eventRepository.findByExternalId("OLD-001")).isEmpty();
    assertThat(eventRepository.findByExternalId("FUT-001")).isPresent();
  }

  @Test
  void findAll_withSpecification_filtersCorrectly() {
    Event zurichEvent =
        Event.builder()
            .externalId("ZH-001")
            .name("Zurich Concert")
            .startDate(LocalDateTime.of(2026, 8, 15, 20, 0))
            .category(category)
            .location(location)
            .build();
    entityManager.persist(zurichEvent);

    Location bernLocation =
        Location.builder().name("Stade de Suisse").city("Bern").country("CH").build();
    bernLocation = entityManager.persist(bernLocation);

    Event bernEvent =
        Event.builder()
            .externalId("BE-001")
            .name("Bern Festival")
            .startDate(LocalDateTime.of(2026, 9, 1, 18, 0))
            .category(category)
            .location(bernLocation)
            .build();
    entityManager.persist(bernEvent);
    entityManager.flush();

    Specification<Event> spec =
        Specification.where(
            (root, query, cb) -> cb.equal(cb.lower(root.get("location").get("city")), "zurich"));

    var result = eventRepository.findAll(spec);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getName()).isEqualTo("Zurich Concert");
  }
}
