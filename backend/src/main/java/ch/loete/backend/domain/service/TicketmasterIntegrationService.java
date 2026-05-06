package ch.loete.backend.domain.service;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.process.client.TicketmasterClient;
import ch.loete.backend.process.client.TicketmasterClient.TicketmasterEvent;
import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.LocationRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketmasterIntegrationService {

  private final TicketmasterClient ticketmasterClient;
  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;
  private final LocationRepository locationRepository;

  private static final int MAX_EVENTS_PER_SYNC = 1000;
  private static final int LOOKAHEAD_DAYS = 90;

  private static final Map<String, String> SEGMENT_TO_CATEGORY =
      Map.ofEntries(
          Map.entry("music", "Konzert"),
          Map.entry("sports", "Sport"),
          Map.entry("arts & theatre", "Theater"),
          Map.entry("arts & theater", "Theater"),
          Map.entry("film", "Sonstiges"),
          Map.entry("miscellaneous", "Sonstiges"),
          Map.entry("comedy", "Comedy"));

  /**
   * Deletes past events, then fetches up to {@value #MAX_EVENTS_PER_SYNC} Swiss events starting in
   * the next {@value #LOOKAHEAD_DAYS} days from Ticketmaster and upserts them by external_id.
   */
  @Transactional
  public int syncUpcomingEvents() {
    LocalDateTime now = LocalDateTime.now();
    int deleted = eventRepository.deleteByStartDateBefore(now);
    if (deleted > 0) {
      log.info("Deleted {} past events", deleted);
    }

    Instant from = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    Instant to = from.plus(LOOKAHEAD_DAYS, ChronoUnit.DAYS);

    List<TicketmasterEvent> tmEvents =
        ticketmasterClient.fetchUpcomingEvents(from, to, MAX_EVENTS_PER_SYNC);

    LocalDateTime windowEnd = LocalDateTime.ofInstant(to, ZoneOffset.UTC);

    int count = 0;
    for (TicketmasterEvent tmEvent : tmEvents) {
      if (tmEvent.startDate() == null
          || tmEvent.startDate().isBefore(now)
          || tmEvent.startDate().isAfter(windowEnd)) {
        continue;
      }
      try {
        upsertEvent(tmEvent);
        count++;
      } catch (Exception e) {
        log.warn(
            "Failed to sync event '{}' (externalId={}): {}",
            tmEvent.name(),
            tmEvent.id(),
            e.getMessage());
      }
    }

    log.info("Synced {} events from Ticketmaster (window {} → {})", count, now, windowEnd);
    return count;
  }

  private void upsertEvent(TicketmasterEvent tmEvent) {
    Category category = findOrCreateCategory(tmEvent.segmentName());
    Location location = findOrCreateLocation(tmEvent);

    Event event =
        eventRepository
            .findByExternalId(tmEvent.id())
            .orElseGet(() -> Event.builder().externalId(tmEvent.id()).build());

    event.setName(tmEvent.name());
    event.setDescription(tmEvent.info());
    event.setImageUrl(tmEvent.imageUrl());
    event.setTicketUrl(tmEvent.url());
    event.setStartDate(tmEvent.startDate());
    event.setCategory(category);
    event.setLocation(location);
    event.setSource("TICKETMASTER");

    eventRepository.save(event);
  }

  private Category findOrCreateCategory(String segmentName) {
    String mapped = "Sonstiges";
    if (segmentName != null && !segmentName.isBlank()) {
      mapped = SEGMENT_TO_CATEGORY.getOrDefault(segmentName.toLowerCase().trim(), "Sonstiges");
    }
    return categoryRepository.findByNameIgnoreCase(mapped).orElseThrow();
  }

  private Location findOrCreateLocation(TicketmasterEvent tmEvent) {
    String venueName = tmEvent.venueName();
    String city = tmEvent.city();

    if (venueName == null || venueName.isBlank()) {
      venueName = "Unknown Venue";
    }
    if (city == null || city.isBlank()) {
      city = "Unknown";
    }

    String finalVenueName = venueName;
    String finalCity = city;

    return locationRepository
        .findByNameAndCity(venueName, city)
        .orElseGet(
            () -> {
              Location location =
                  Location.builder()
                      .name(finalVenueName)
                      .city(finalCity)
                      .country(tmEvent.countryCode() != null ? tmEvent.countryCode() : "CH")
                      .latitude(tmEvent.latitude())
                      .longitude(tmEvent.longitude())
                      .build();
              return locationRepository.save(location);
            });
  }
}
