package ch.loete.backend.domain.service;

import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.FavoriteRepository;
import ch.loete.backend.process.repository.LocationRepository;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.request.CreateEventRequest;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.response.EventDetailResponse;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service für Event-bezogene Geschaeftslogik.
 *
 * <p>Stellt paginierte Event-Listen mit Filterung (Kategorie, Stadt, Datum, Textsuche) sowie die
 * Detailansicht einzelner Events bereit. Prüft bei authentifizierten Benutzern den Favoriten-Status
 * und ermöglicht angemeldeten Benutzern das Erstellen eigener Events.
 */
@Service
@RequiredArgsConstructor
public class EventService {

  /** Quelle für von Benutzern erstellte Events. */
  public static final String SOURCE_USER = "USER";

  /** Repository für den Zugriff auf Event-Daten. */
  private final EventRepository eventRepository;

  /** Repository für die Favoriten-Abfrage (Favoritenstatus). */
  private final FavoriteRepository favoriteRepository;

  /** Repository für die Aufloesung der Kategorie beim Erstellen eines Events. */
  private final CategoryRepository categoryRepository;

  /** Repository für das Finden bzw. Anlegen des Veranstaltungsortes. */
  private final LocationRepository locationRepository;

  /** Repository für die Aufloesung des erstellenden Benutzers. */
  private final UserRepository userRepository;

  /**
   * Gibt eine paginierte, gefilterte Liste von Events zurück.
   *
   * <p>Die Events werden nach Startdatum aufsteigend sortiert. Filter koennen Kategorie, Stadt,
   * Datumsbereich und Textsuche umfassen.
   *
   * @param filter die Filterkriterien und Paginierungsparameter
   * @return die paginierte Event-Liste
   */
  @Transactional(readOnly = true)
  public PagedResponse<EventResponse> getEvents(EventFilterRequest filter) {
    List<Specification<Event>> specs =
        Stream.of(
                EventSpecifications.hasCategory(filter.categoryId()),
                EventSpecifications.inCity(filter.city()),
                EventSpecifications.startsAfter(filter.dateFrom()),
                EventSpecifications.startsBefore(filter.dateTo()),
                EventSpecifications.nameContains(filter.search()))
            .filter(Objects::nonNull)
            .toList();
    Specification<Event> spec = specs.isEmpty() ? null : Specification.allOf(specs);

    Page<Event> result =
        eventRepository.findAll(
            spec, PageRequest.of(filter.page(), filter.size(), Sort.by("startDate").ascending()));

    return new PagedResponse<>(
        result.getContent().stream().map(this::toEventResponse).toList(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages(),
        result.isLast());
  }

  /**
   * Gibt die Detailansicht eines Events zurück.
   *
   * @param id die Event-ID
   * @param userId die Benutzer-ID (oder {@code null} für anonyme Nutzer)
   * @return die Detailantwort mit Favoritenstatus
   * @throws ResourceNotFoundException wenn das Event nicht existiert
   */
  @Transactional(readOnly = true)
  public EventDetailResponse getEvent(String id, String userId) {
    Event event =
        eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event", id));

    boolean favorited = userId != null && favoriteRepository.existsByUser_IdAndEvent_Id(userId, id);

    return toEventDetailResponse(event, favorited);
  }

  /**
   * Erstellt ein neues Event im Namen eines Benutzers.
   *
   * <p>Der Veranstaltungsort wird anhand von Name und Stadt wiederverwendet oder neu angelegt. Das
   * Embedding für die semantische Suche wird vom regulären Embedding-Job nachträglich erzeugt.
   *
   * @param request die Event-Daten
   * @param userId die ID des erstellenden Benutzers
   * @return die Detailansicht des erstellten Events
   * @throws ResourceNotFoundException wenn Benutzer oder Kategorie nicht existieren
   */
  @Transactional
  public EventDetailResponse createEvent(CreateEventRequest request, String userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    Long categoryId = request.categoryId();
    Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId.toString()));

    String locationName = request.locationName().trim();
    String city = request.city().trim();
    Location location =
        locationRepository
            .findByNameAndCity(locationName, city)
            .orElseGet(
                () ->
                    locationRepository.save(
                        Location.builder().name(locationName).city(city).build()));

    Event event =
        Event.builder()
            .name(request.name().trim())
            .description(blankToNull(request.description()))
            .imageUrl(blankToNull(request.imageUrl()))
            .ticketUrl(blankToNull(request.ticketUrl()))
            .startDate(request.startDate())
            .endDate(request.endDate())
            .category(category)
            .location(location)
            .createdBy(user)
            .source(SOURCE_USER)
            .build();

    return toEventDetailResponse(eventRepository.save(event), false);
  }

  /**
   * Trimmt einen optionalen Text und wandelt leere Werte in {@code null} um.
   *
   * @param value der Eingabewert
   * @return der getrimmte Wert oder {@code null}
   */
  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  /**
   * Konvertiert eine Event-Entität in ein EventResponse-DTO.
   *
   * @param event die Event-Entität
   * @return das Response-DTO
   */
  private EventResponse toEventResponse(Event event) {
    return new EventResponse(
        event.getId(),
        event.getName(),
        event.getImageUrl(),
        event.getStartDate(),
        event.getCategory() != null ? event.getCategory().getName() : null,
        event.getLocation() != null ? event.getLocation().getName() : null,
        event.getLocation() != null ? event.getLocation().getCity() : null);
  }

  /**
   * Konvertiert eine Event-Entität in ein EventDetailResponse-DTO.
   *
   * @param event die Event-Entität
   * @param favorited ob das Event vom Benutzer favorisiert ist
   * @return das Detail-Response-DTO
   */
  private EventDetailResponse toEventDetailResponse(Event event, boolean favorited) {
    return new EventDetailResponse(
        event.getId(),
        event.getName(),
        event.getDescription(),
        event.getImageUrl(),
        event.getTicketUrl(),
        event.getStartDate(),
        event.getEndDate(),
        event.getCategory() != null ? event.getCategory().getName() : null,
        event.getCategory() != null ? event.getCategory().getSlug() : null,
        event.getLocation() != null ? event.getLocation().getName() : null,
        event.getLocation() != null ? event.getLocation().getCity() : null,
        event.getLocation() != null ? event.getLocation().getCountry() : null,
        event.getLocation() != null ? event.getLocation().getLatitude() : null,
        event.getLocation() != null ? event.getLocation().getLongitude() : null,
        favorited);
  }
}
