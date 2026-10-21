package ch.loete.backend.process.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class TicketmasterClient {

  private final RestClient restClient;
  private final String apiKey;

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

  public List<TicketmasterEvent> fetchEvents() {
    if (!StringUtils.hasText(apiKey)) {
      log.warn("Ticketmaster API key is not configured. Skipping event fetch.");
      return Collections.emptyList();
    }

    try {
      JsonNode response =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/events.json")
                          .queryParam("apikey", apiKey)
                          .queryParam("countryCode", "CH")
                          .queryParam("size", "50")
                          .build())
              .retrieve()
              .body(JsonNode.class);

      return parseEvents(response);
    } catch (Exception e) {
      log.error("Failed to fetch events from Ticketmaster: {}", e.getMessage(), e);
      return Collections.emptyList();
    }
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
