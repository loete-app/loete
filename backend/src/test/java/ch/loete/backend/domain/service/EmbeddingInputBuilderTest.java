package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EmbeddingInputBuilderTest {

  private final EmbeddingInputBuilder builder = new EmbeddingInputBuilder();

  @Test
  void buildEmbeddingInput_fullyPopulatedEvent() {
    Category category = Category.builder().id(1L).name("Konzert").slug("konzert").build();
    Location location =
        Location.builder().id(1L).name("Hallenstadion").city("Zurich").country("CH").build();
    Event event =
        Event.builder()
            .id("abc12345")
            .name("Jazz Night")
            .description("An evening of smooth jazz under the stars.")
            .startDate(LocalDateTime.of(2026, 7, 15, 20, 30))
            .category(category)
            .location(location)
            .build();

    String result = builder.buildEmbeddingInput(event);

    assertThat(result).contains("Jazz Night");
    assertThat(result).contains("Category: Konzert");
    assertThat(result).contains("Venue: Hallenstadion");
    assertThat(result).contains("City: Zurich");
    assertThat(result).contains("Country: CH");
    assertThat(result).contains("Season: Summer");
    assertThat(result).contains("Time of day: evening");
    assertThat(result).contains("An evening of smooth jazz under the stars.");
  }

  @Test
  void buildEmbeddingInput_sparseEvent() {
    Event event = Event.builder().id("xyz98765").name("Mystery Event").build();

    String result = builder.buildEmbeddingInput(event);

    assertThat(result).isEqualTo("Mystery Event");
  }

  @Test
  void buildEmbeddingInput_truncatesLongDescription() {
    String longDesc = "A".repeat(600);
    Event event =
        Event.builder().id("trunc001").name("Long Desc Event").description(longDesc).build();

    String result = builder.buildEmbeddingInput(event);

    assertThat(result).contains("A".repeat(500));
    assertThat(result).doesNotContain("A".repeat(501));
  }

  @Test
  void buildEmbeddingInput_seasonAndTimeOfDay() {
    Event winterMorning =
        Event.builder()
            .id("s1")
            .name("Winter Brunch")
            .startDate(LocalDateTime.of(2026, 1, 15, 10, 0))
            .build();
    assertThat(builder.buildEmbeddingInput(winterMorning)).contains("Season: Winter");
    assertThat(builder.buildEmbeddingInput(winterMorning)).contains("Time of day: morning");

    Event springAfternoon =
        Event.builder()
            .id("s2")
            .name("Spring Workshop")
            .startDate(LocalDateTime.of(2026, 4, 10, 14, 0))
            .build();
    assertThat(builder.buildEmbeddingInput(springAfternoon)).contains("Season: Spring");
    assertThat(builder.buildEmbeddingInput(springAfternoon)).contains("Time of day: afternoon");

    Event autumnNight =
        Event.builder()
            .id("s3")
            .name("Autumn Rave")
            .startDate(LocalDateTime.of(2026, 10, 31, 23, 0))
            .build();
    assertThat(builder.buildEmbeddingInput(autumnNight)).contains("Season: Autumn");
    assertThat(builder.buildEmbeddingInput(autumnNight)).contains("Time of day: night");
  }

  @Test
  void buildEmbeddingInput_locationWithNullFields() {
    Location location = Location.builder().id(1L).name(null).city("Basel").country(null).build();
    Event event =
        Event.builder().id("loc01").name("Partial Location Event").location(location).build();

    String result = builder.buildEmbeddingInput(event);

    assertThat(result).contains("City: Basel");
    assertThat(result).doesNotContain("Venue:");
    assertThat(result).doesNotContain("Country:");
  }

  @Test
  void buildEmbeddingInput_nullName() {
    Event event = Event.builder().id("nn01").name(null).build();

    String result = builder.buildEmbeddingInput(event);

    assertThat(result).isEmpty();
  }

  @Test
  void buildEmbeddingInput_categoryWithNullName() {
    Category category = Category.builder().id(1L).name(null).slug("test").build();
    Event event = Event.builder().id("cn01").name("Event").category(category).build();

    String result = builder.buildEmbeddingInput(event);

    assertThat(result).isEqualTo("Event");
    assertThat(result).doesNotContain("Category:");
  }

  @Test
  void buildEmbeddingInput_decemberWinter() {
    Event event =
        Event.builder()
            .id("dec01")
            .name("Christmas Market")
            .startDate(LocalDateTime.of(2026, 12, 20, 16, 0))
            .build();

    String result = builder.buildEmbeddingInput(event);

    assertThat(result).contains("Season: Winter");
    assertThat(result).contains("Time of day: afternoon");
  }
}
