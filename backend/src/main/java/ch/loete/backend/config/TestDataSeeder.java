package ch.loete.backend.config;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.FavoriteRepository;
import ch.loete.backend.process.repository.LocationRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("testdata")
@RequiredArgsConstructor
public class TestDataSeeder implements CommandLineRunner {

  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;
  private final LocationRepository locationRepository;
  private final FavoriteRepository favoriteRepository;

  private static final String[] CITIES = {"Bern", "Zürich", "Basel", "Genf"};
  private static final String[] VENUE_NAMES = {
    "Stade de Suisse", "Hallenstadion", "St. Jakob-Park", "Arena Genève"
  };

  @Override
  public void run(String... args) {
    String mode = System.getenv().getOrDefault("LOETE_SEED", "small");

    switch (mode.toLowerCase()) {
      case "clear" -> clear();
      case "large" -> seedLarge();
      default -> seedSmall();
    }
  }

  void seedSmall() {
    seed(10);
  }

  void seedLarge() {
    seed(500);
  }

  void clear() {
    favoriteRepository.deleteAll();
    eventRepository.deleteAll();
    log.info("Cleared all events and favorites");
  }

  private void seed(int count) {
    List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
    if (categories.isEmpty()) {
      log.warn("No categories found. Run Flyway migrations first.");
      return;
    }

    LocalDateTime baseDate = LocalDateTime.now().plusDays(1);

    for (int i = 1; i <= count; i++) {
      int cityIdx = (i - 1) % CITIES.length;
      String city = CITIES[cityIdx];
      String venueName = VENUE_NAMES[cityIdx];

      Location location =
          locationRepository
              .findByNameAndCity(venueName, city)
              .orElseGet(
                  () ->
                      locationRepository.save(
                          Location.builder().name(venueName).city(city).country("CH").build()));

      Category category = categories.get((i - 1) % categories.size());
      LocalDateTime startDate = baseDate.plusDays((i - 1) % 30).plusHours((i % 5) + 18);

      String externalId = "TEST-" + String.format("%04d", i);

      if (eventRepository.findByExternalId(externalId).isPresent()) {
        continue;
      }

      eventRepository.save(
          Event.builder()
              .externalId(externalId)
              .name("Test-Event " + i)
              .description("Beschreibung für Test-Event " + i)
              .startDate(startDate)
              .category(category)
              .location(location)
              .source("TESTDATA")
              .build());
    }

    log.info("Seeded {} test events", count);
  }
}
