package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.EventService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.response.EventDetailResponse;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für Event-Endpunkte.
 *
 * <p>Stellt öffentliche GET-Endpunkte für die paginierte Event-Liste (mit Filterung) und die
 * Event-Detailansicht bereit.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

  /** Service für Event-Geschaeftslogik. */
  private final EventService eventService;

  /** Repository für die Aufloesung des Benutzers aus dem Authentication-Objekt. */
  private final UserRepository userRepository;

  /**
   * Gibt eine paginierte, gefilterte Liste von Events zurück.
   *
   * @param categoryId optionale Kategorie-ID
   * @param city optionaler Stadtfilter
   * @param dateFrom optionales Startdatum (ISO-Format)
   * @param dateTo optionales Enddatum (ISO-Format)
   * @param search optionaler Suchbegriff
   * @param page Seitennummer (Standard: 0)
   * @param size Seitengrösse (Standard: 20)
   * @return die paginierte Event-Liste
   */
  @GetMapping
  public ResponseEntity<PagedResponse<EventResponse>> getEvents(
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String city,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateTo,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    EventFilterRequest filter =
        new EventFilterRequest(categoryId, city, dateFrom, dateTo, search, page, size);
    return ResponseEntity.ok(eventService.getEvents(filter));
  }

  /**
   * Gibt die Detailansicht eines Events zurück.
   *
   * <p>Bei authentifizierten Benutzern wird zusaetzlich der Favoriten-Status ermittelt.
   *
   * @param id die Event-ID
   * @param auth das Authentication-Objekt (oder {@code null} für anonyme Nutzer)
   * @return die Event-Detailansicht
   */
  @GetMapping("/{id}")
  public ResponseEntity<EventDetailResponse> getEvent(
      @PathVariable String id, Authentication auth) {
    String userId = null;
    if (auth != null) {
      userId = userRepository.findByEmail(auth.getName()).map(user -> user.getId()).orElse(null);
    }
    return ResponseEntity.ok(eventService.getEvent(id, userId));
  }
}
