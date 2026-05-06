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
import ch.loete.backend.web.dto.response.CategoryResponse;
import ch.loete.backend.web.dto.response.EventDetailResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class EventApiIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private EventRepository eventRepository;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private LocationRepository locationRepository;

  private Event zurichEvent;
  private Event bernEvent;

  @BeforeEach
  void setUp() {
    eventRepository.deleteAll();
    locationRepository.deleteAll();

    Category konzert = categoryRepository.findBySlug("konzert").orElseThrow();

    Location zurichLocation =
        locationRepository.save(
            Location.builder().name("Hallenstadion").city("Zurich").country("CH").build());
    Location bernLocation =
        locationRepository.save(
            Location.builder().name("Stade de Suisse").city("Bern").country("CH").build());

    zurichEvent =
        eventRepository.save(
            Event.builder()
                .externalId("INT-ZH-001")
                .name("Zurich Rock Night")
                .description("A great rock concert")
                .startDate(LocalDateTime.of(2026, 8, 15, 20, 0))
                .category(konzert)
                .location(zurichLocation)
                .build());

    bernEvent =
        eventRepository.save(
            Event.builder()
                .externalId("INT-BE-001")
                .name("Bern Jazz Evening")
                .startDate(LocalDateTime.of(2026, 9, 1, 19, 0))
                .category(konzert)
                .location(bernLocation)
                .build());
  }

  @Test
  void getEvents_returns200WithEvents() {
    ResponseEntity<PagedResponse<Object>> response =
        restTemplate.exchange(
            "/events", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().content()).hasSize(2);
  }

  @Test
  void getEvents_filtersByCity() {
    ResponseEntity<PagedResponse<Object>> response =
        restTemplate.exchange(
            "/events?city=Zurich", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().content()).hasSize(1);
  }

  @Test
  void getEvent_returnsEventDetail() {
    ResponseEntity<EventDetailResponse> response =
        restTemplate.getForEntity("/events/" + zurichEvent.getId(), EventDetailResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().name()).isEqualTo("Zurich Rock Night");
    assertThat(response.getBody().description()).isEqualTo("A great rock concert");
    assertThat(response.getBody().city()).isEqualTo("Zurich");
    assertThat(response.getBody().favorited()).isFalse();
  }

  @Test
  void getEvent_returns404ForNonexistent() {
    ResponseEntity<String> response = restTemplate.getForEntity("/events/notfound", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getCategories_returnsPreSeededCategories() {
    ResponseEntity<CategoryResponse[]> response =
        restTemplate.getForEntity("/categories", CategoryResponse[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSize(7);

    // Verify sorted alphabetically
    assertThat(response.getBody()[0].name()).isEqualTo("Comedy");
  }
}
