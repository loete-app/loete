package ch.loete.backend.process.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.testconfig.TestcontainersConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class LocationRepositoryTest {

  @Autowired private LocationRepository locationRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  void findByNameAndCity_returnsMatchingLocation() {
    Location location =
        Location.builder().name("Hallenstadion").city("Zurich").country("CH").build();
    entityManager.persist(location);
    entityManager.flush();

    Optional<Location> result = locationRepository.findByNameAndCity("Hallenstadion", "Zurich");

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Hallenstadion");
    assertThat(result.get().getCity()).isEqualTo("Zurich");
  }

  @Test
  void findByNameAndCity_returnsEmptyWhenNotFound() {
    Optional<Location> result = locationRepository.findByNameAndCity("Nonexistent", "Nowhere");

    assertThat(result).isEmpty();
  }

  @Test
  void findDistinctCitiesWithEvents_returnsCitiesWithLinkedEvents() {
    Category category =
        entityManager
            .getEntityManager()
            .createQuery("SELECT c FROM Category c WHERE c.slug = 'konzert'", Category.class)
            .getSingleResult();

    Location zurichLocation =
        Location.builder().name("Hallenstadion").city("Zurich").country("CH").build();
    zurichLocation = entityManager.persist(zurichLocation);

    Location bernLocation =
        Location.builder().name("Stade de Suisse").city("Bern").country("CH").build();
    bernLocation = entityManager.persist(bernLocation);

    // Location without events should not appear
    Location emptyLocation =
        Location.builder().name("Empty Venue").city("Geneva").country("CH").build();
    entityManager.persist(emptyLocation);

    Event event1 =
        Event.builder()
            .externalId("TM-L001")
            .name("Zurich Concert")
            .startDate(LocalDateTime.of(2026, 8, 15, 20, 0))
            .category(category)
            .location(zurichLocation)
            .build();
    entityManager.persist(event1);

    Event event2 =
        Event.builder()
            .externalId("TM-L002")
            .name("Bern Show")
            .startDate(LocalDateTime.of(2026, 9, 1, 19, 0))
            .category(category)
            .location(bernLocation)
            .build();
    entityManager.persist(event2);
    entityManager.flush();

    List<String> cities = locationRepository.findDistinctCitiesWithEvents();

    assertThat(cities).containsExactly("Bern", "Zurich");
    assertThat(cities).doesNotContain("Geneva");
  }
}
