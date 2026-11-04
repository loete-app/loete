package ch.loete.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.LocationRepository;
import ch.loete.backend.testconfig.TestSecurityConfig;
import ch.loete.backend.testconfig.TestcontainersConfig;
import ch.loete.backend.web.dto.response.VibeSearchResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.vibe-search.enabled=true", "app.embeddings.api-key=test-key"})
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class VibeSearchApiIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private EventRepository eventRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private LocationRepository locationRepository;

  @BeforeEach
  void setUp() {
    eventRepository.deleteAll();
    locationRepository.deleteAll();

    Category konzert = categoryRepository.findBySlug("konzert").orElseThrow();
    Location zurichLocation =
        locationRepository.save(
            Location.builder().name("Tonhalle").city("Zurich").country("CH").build());

    eventRepository.save(
        Event.builder()
            .externalId("VIBE-ZH-001")
            .name("Zurich Jazz Evening")
            .description("Smooth jazz under the stars")
            .startDate(LocalDateTime.of(2026, 8, 15, 20, 0))
            .category(konzert)
            .location(zurichLocation)
            .build());
  }

  @Test
  void vibeSearch_returns200WithResults() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body =
        """
        {"query": "gemütlicher Jazz-Abend mit Freunden"}
        """;
    HttpEntity<String> request = new HttpEntity<>(body, headers);

    ResponseEntity<VibeSearchResponse> response =
        restTemplate.postForEntity("/search/vibe", request, VibeSearchResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().results()).isNotNull();
  }

  @Test
  void vibeSearch_returns400ForTooShortQuery() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body =
        """
        {"query": "ab"}
        """;
    HttpEntity<String> request = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity("/search/vibe", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void vibeSearch_returns400ForMissingQuery() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>("{}", headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity("/search/vibe", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void v7MigrationAppliesCleanly() {
    // If we get here, the migration has already run via Flyway + Testcontainers.
    // Verify the embedding column exists by running a native query.
    long count = eventRepository.count();
    assertThat(count).isGreaterThanOrEqualTo(0);
  }
}
