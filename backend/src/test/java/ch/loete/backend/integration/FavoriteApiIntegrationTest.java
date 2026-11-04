package ch.loete.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.FavoriteRepository;
import ch.loete.backend.process.repository.LocationRepository;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.testconfig.TestSecurityConfig;
import ch.loete.backend.testconfig.TestcontainersConfig;
import ch.loete.backend.web.dto.response.FavoriteResponse;
import ch.loete.backend.web.dto.response.MigrateFavoritesResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class FavoriteApiIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private EventRepository eventRepository;
  @Autowired private FavoriteRepository favoriteRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private LocationRepository locationRepository;
  @Autowired private UserRepository userRepository;

  private Event testEvent;
  private Event testEvent2;
  private User testUser;

  @BeforeEach
  void setUp() {
    favoriteRepository.deleteAll();
    eventRepository.deleteAll();
    locationRepository.deleteAll();
    userRepository.deleteAll();

    testUser =
        userRepository.save(
            User.builder()
                .email(TestSecurityConfig.TEST_USER_EMAIL)
                .username("favtester")
                .passwordHash("$2a$10$hashed")
                .build());

    Category konzert = categoryRepository.findBySlug("konzert").orElseThrow();
    Location location =
        locationRepository.save(
            Location.builder().name("Hallenstadion").city("Zurich").country("CH").build());

    testEvent =
        eventRepository.save(
            Event.builder()
                .externalId("FAV-INT-001")
                .name("Favorite Test Event")
                .startDate(LocalDateTime.of(2026, 8, 15, 20, 0))
                .category(konzert)
                .location(location)
                .build());

    testEvent2 =
        eventRepository.save(
            Event.builder()
                .externalId("FAV-INT-002")
                .name("Second Test Event")
                .startDate(LocalDateTime.of(2026, 9, 1, 19, 0))
                .category(konzert)
                .location(location)
                .build());
  }

  @Test
  void addFavorite_returns200() {
    ResponseEntity<FavoriteResponse> response =
        restTemplate.exchange(
            "/favorites/" + testEvent.getId(), HttpMethod.POST, null, FavoriteResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().eventId()).isEqualTo(testEvent.getId());
    assertThat(response.getBody().name()).isEqualTo("Favorite Test Event");
  }

  @Test
  void addFavorite_isIdempotent() {
    restTemplate.exchange(
        "/favorites/" + testEvent.getId(), HttpMethod.POST, null, FavoriteResponse.class);

    ResponseEntity<FavoriteResponse> second =
        restTemplate.exchange(
            "/favorites/" + testEvent.getId(), HttpMethod.POST, null, FavoriteResponse.class);

    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(second.getBody()).isNotNull();
    assertThat(second.getBody().eventId()).isEqualTo(testEvent.getId());
  }

  @Test
  void getFavorites_returnsAddedFavorite() {
    restTemplate.exchange(
        "/favorites/" + testEvent.getId(), HttpMethod.POST, null, FavoriteResponse.class);

    ResponseEntity<FavoriteResponse[]> response =
        restTemplate.exchange("/favorites", HttpMethod.GET, null, FavoriteResponse[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody()[0].eventId()).isEqualTo(testEvent.getId());
  }

  @Test
  void getFavoriteIds_returnsEventId() {
    restTemplate.exchange(
        "/favorites/" + testEvent.getId(), HttpMethod.POST, null, FavoriteResponse.class);

    ResponseEntity<String[]> response =
        restTemplate.exchange("/favorites/ids", HttpMethod.GET, null, String[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).contains(testEvent.getId());
  }

  @Test
  void removeFavorite_returns204() {
    restTemplate.exchange(
        "/favorites/" + testEvent.getId(), HttpMethod.POST, null, FavoriteResponse.class);

    ResponseEntity<Void> deleteResponse =
        restTemplate.exchange(
            "/favorites/" + testEvent.getId(), HttpMethod.DELETE, null, Void.class);

    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ResponseEntity<FavoriteResponse[]> listResponse =
        restTemplate.exchange("/favorites", HttpMethod.GET, null, FavoriteResponse[].class);

    assertThat(listResponse.getBody()).isEmpty();
  }

  @Test
  void migrateFavorites_migratesNewAndSkipsExisting() {
    restTemplate.exchange(
        "/favorites/" + testEvent.getId(), HttpMethod.POST, null, FavoriteResponse.class);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"eventIds\":[\"" + testEvent.getId() + "\",\"" + testEvent2.getId() + "\"]}";

    ResponseEntity<MigrateFavoritesResponse> response =
        restTemplate.exchange(
            "/favorites/migrate",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            MigrateFavoritesResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().migrated()).containsExactly(testEvent2.getId());
    assertThat(response.getBody().skipped()).containsExactly(testEvent.getId());
  }
}
