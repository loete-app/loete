package ch.loete.backend.process.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class TicketmasterClient {

  private final RestClient restClient;
  private final String apiKey;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public record TicketmasterEvent(
      String id,
      String name,
      String info,
      String url,
      String imageUrl,
      LocalDateTime startDate,
      String segmentName,
      String venueName,
      String city,
      String countryCode,
      Double latitude,
      Double longitude) {}

  public TicketmasterClient(
      @Value("${app.ticketmaster.base-url}") String baseUrl,
      @Value("${app.ticketmaster.api-key}") String apiKey) {
    this.apiKey = apiKey;
    this.restClient = RestClient.builder().baseUrl(baseUrl).build();
  }

  private static final int PAGE_SIZE = 200;

  /**
   * Fetches events from Ticketmaster within the given UTC window, up to {@code maxResults}.
   * Pages through results (size=200, the API max). Returns whatever it managed to fetch — failures
   * on a single page are logged and the partial result is returned.
   */
  public List<TicketmasterEvent> fetchUpcomingEvents(
      Instant startDateTime, Instant endDateTime, int maxResults) {
    if (!StringUtils.hasText(apiKey)) {
      log.warn("Ticketmaster API key is not configured. Skipping event fetch.");
      return Collections.emptyList();
    }

    String start =
        DateTimeFormatter.ISO_INSTANT.format(startDateTime.truncatedTo(ChronoUnit.SECONDS));
    String end =
        DateTimeFormatter.ISO_INSTANT.format(endDateTime.truncatedTo(ChronoUnit.SECONDS));

    List<TicketmasterEvent> all = new ArrayList<>();
    int page = 0;

    while (all.size() < maxResults) {
      final int currentPage = page;
      try {
        String body =
            restClient
                .get()
                .uri(
                    uriBuilder ->
                        uriBuilder
                            .path("/events.json")
                            .queryParam("apikey", apiKey)
                            .queryParam("countryCode", "CH")
                            .queryParam("startDateTime", start)
                            .queryParam("endDateTime", end)
                            .queryParam("size", String.valueOf(PAGE_SIZE))
                            .queryParam("page", String.valueOf(currentPage))
                            .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        if (body == null || body.isBlank()) {
          log.warn("Ticketmaster returned empty body for page {}", currentPage);
          break;
        }

        JsonNode response = objectMapper.readTree(body);

        List<TicketmasterEvent> pageEvents = parseEvents(response);
        if (pageEvents.isEmpty()) {
          break;
        }
        all.addAll(pageEvents);

        int totalPages = response.path("page").path("totalPages").asInt(0);
        if (currentPage + 1 >= totalPages) {
          break;
        }
        page++;
      } catch (Exception e) {
        log.error(
            "Failed to fetch Ticketmaster events page {}: {}. Returning {} events fetched so far.",
            currentPage,
            e.getMessage(),
            all.size());
        break;
      }
    }

    return all.size() > maxResults ? all.subList(0, maxResults) : all;
  }

  private List<TicketmasterEvent> parseEvents(JsonNode response) {
    List<TicketmasterEvent> events = new ArrayList<>();

    if (response == null || !response.has("_embedded")) {
      return events;
    }

    JsonNode embeddedEvents = response.path("_embedded").path("events");
    if (!embeddedEvents.isArray()) {
      return events;
    }

    for (JsonNode eventNode : embeddedEvents) {
      try {
        events.add(parseEvent(eventNode));
      } catch (Exception e) {
        log.warn("Failed to parse Ticketmaster event: {}. Skipping.", e.getMessage());
      }
    }

    return events;
  }

  private TicketmasterEvent parseEvent(JsonNode node) {
    String id = node.path("id").asText(null);
    String name = node.path("name").asText(null);
    String info = node.path("info").asText(null);
    String url = node.path("url").asText(null);

    String imageUrl = null;
    JsonNode images = node.path("images");
    if (images.isArray() && !images.isEmpty()) {
      imageUrl = images.get(0).path("url").asText(null);
    }

    LocalDateTime startDate = null;
    JsonNode dates = node.path("dates").path("start");
    String localDate = dates.path("localDate").asText(null);
    String localTime = dates.path("localTime").asText(null);
    if (localDate != null) {
      String dateTimeStr =
          localTime != null ? localDate + "T" + localTime : localDate + "T00:00:00";
      startDate = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    String segmentName = null;
    JsonNode classifications = node.path("classifications");
    if (classifications.isArray() && !classifications.isEmpty()) {
      segmentName = classifications.get(0).path("segment").path("name").asText(null);
    }

    String venueName = null;
    String city = null;
    String countryCode = null;
    Double latitude = null;
    Double longitude = null;
    JsonNode venues = node.path("_embedded").path("venues");
    if (venues.isArray() && !venues.isEmpty()) {
      JsonNode venue = venues.get(0);
      venueName = venue.path("name").asText(null);
      city = venue.path("city").path("name").asText(null);
      countryCode = venue.path("country").path("countryCode").asText(null);
      JsonNode location = venue.path("location");
      if (location.has("latitude")) {
        latitude = parseDouble(location.path("latitude").asText(null));
      }
      if (location.has("longitude")) {
        longitude = parseDouble(location.path("longitude").asText(null));
      }
    }

    return new TicketmasterEvent(
        id,
        name,
        info,
        url,
        imageUrl,
        startDate,
        segmentName,
        venueName,
        city,
        countryCode,
        latitude,
        longitude);
  }

  private Double parseDouble(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
