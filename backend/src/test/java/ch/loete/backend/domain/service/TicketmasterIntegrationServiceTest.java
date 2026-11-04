package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketmasterIntegrationServiceTest {

  @Mock private TicketmasterClient ticketmasterClient;

  @Mock private EventRepository eventRepository;

  @Mock private CategoryRepository categoryRepository;

  @Mock private LocationRepository locationRepository;

  @InjectMocks private TicketmasterIntegrationService integrationService;

  private TicketmasterEvent buildTmEvent(
      String id, String name, LocalDateTime startDate, String segment, String venue, String city) {
    return new TicketmasterEvent(
        id,
        name,
        "Some info",
        "https://tickets.example.com/" + id,
        "https://img.example.com/" + id + ".jpg",
        startDate,
        segment,
        venue,
        city,
        "CH",
        47.37,
        8.54);
  }

  @Test
  void syncUpcomingEvents_deletesPastEventsAndSyncsNewOnes() {
    given(eventRepository.deleteByStartDateBefore(any(LocalDateTime.class))).willReturn(2);

    LocalDateTime futureDate = LocalDateTime.now().plusDays(10);
    TicketmasterEvent tmEvent =
        buildTmEvent("tm-1", "Concert", futureDate, "Music", "Arena", "Zurich");

    given(ticketmasterClient.fetchUpcomingEvents(any(Instant.class), any(Instant.class), eq(1000)))
        .willReturn(List.of(tmEvent));

    Category category = Category.builder().id(1L).name("Konzert").slug("konzert").build();
    given(categoryRepository.findByNameIgnoreCase("Konzert")).willReturn(Optional.of(category));

    Location location =
        Location.builder().id(1L).name("Arena").city("Zurich").country("CH").build();
    given(locationRepository.findByNameAndCity("Arena", "Zurich"))
        .willReturn(Optional.of(location));

    Event existingEvent = Event.builder().externalId("tm-1").build();
    given(eventRepository.findByExternalId("tm-1")).willReturn(Optional.of(existingEvent));
    given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

    int count = integrationService.syncUpcomingEvents();

    assertThat(count).isEqualTo(1);
    then(eventRepository).should().deleteByStartDateBefore(any(LocalDateTime.class));
    then(eventRepository).should().save(any(Event.class));
  }

  @Test
  void syncUpcomingEvents_skipsEventsWithNullStartDate() {
    given(eventRepository.deleteByStartDateBefore(any(LocalDateTime.class))).willReturn(0);

    TicketmasterEvent tmEvent =
        new TicketmasterEvent(
            "tm-2",
            "No Date Event",
            null,
            "https://example.com",
            null,
            null,
            "Music",
            "Arena",
            "Zurich",
            "CH",
            null,
            null);

    given(ticketmasterClient.fetchUpcomingEvents(any(Instant.class), any(Instant.class), eq(1000)))
        .willReturn(List.of(tmEvent));

    int count = integrationService.syncUpcomingEvents();

    assertThat(count).isZero();
    then(eventRepository).should(never()).save(any(Event.class));
  }

  @Test
  void syncUpcomingEvents_mapsArtsAndTheatreToTheater() {
    given(eventRepository.deleteByStartDateBefore(any(LocalDateTime.class))).willReturn(0);

    LocalDateTime futureDate = LocalDateTime.now().plusDays(5);
    TicketmasterEvent tmEvent =
        buildTmEvent("tm-3", "Comedy Show", futureDate, "Arts & Theatre", "Club", "Basel");

    given(ticketmasterClient.fetchUpcomingEvents(any(Instant.class), any(Instant.class), eq(1000)))
        .willReturn(List.of(tmEvent));

    Category theater = Category.builder().id(3L).name("Theater").slug("theater").build();
    given(categoryRepository.findByNameIgnoreCase("Theater")).willReturn(Optional.of(theater));

    Location location = Location.builder().id(1L).name("Club").city("Basel").country("CH").build();
    given(locationRepository.findByNameAndCity("Club", "Basel")).willReturn(Optional.of(location));

    given(eventRepository.findByExternalId("tm-3")).willReturn(Optional.empty());
    given(eventRepository.save(any(Event.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    int count = integrationService.syncUpcomingEvents();

    assertThat(count).isEqualTo(1);
    then(categoryRepository).should(never()).save(any(Category.class));
  }

  @Test
  void syncUpcomingEvents_mapsMusicToKonzert() {
    given(eventRepository.deleteByStartDateBefore(any(LocalDateTime.class))).willReturn(0);

    LocalDateTime futureDate = LocalDateTime.now().plusDays(5);
    TicketmasterEvent tmEvent =
        buildTmEvent("tm-4", "Rock Concert", futureDate, "Music", "Hall", "Bern");

    given(ticketmasterClient.fetchUpcomingEvents(any(Instant.class), any(Instant.class), eq(1000)))
        .willReturn(List.of(tmEvent));

    Category existing = Category.builder().id(1L).name("Konzert").slug("konzert").build();
    given(categoryRepository.findByNameIgnoreCase("Konzert")).willReturn(Optional.of(existing));

    Location location = Location.builder().id(2L).name("Hall").city("Bern").country("CH").build();
    given(locationRepository.findByNameAndCity("Hall", "Bern")).willReturn(Optional.of(location));

    given(eventRepository.findByExternalId("tm-4")).willReturn(Optional.empty());
    given(eventRepository.save(any(Event.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    int count = integrationService.syncUpcomingEvents();

    assertThat(count).isEqualTo(1);
    then(categoryRepository).should(never()).save(any(Category.class));
  }

  @Test
  void syncUpcomingEvents_createsLocationWithDefaultsForNullVenueAndCity() {
    given(eventRepository.deleteByStartDateBefore(any(LocalDateTime.class))).willReturn(0);

    LocalDateTime futureDate = LocalDateTime.now().plusDays(5);
    TicketmasterEvent tmEvent =
        new TicketmasterEvent(
            "tm-5",
            "Secret Event",
            "No venue info",
            "https://example.com",
            null,
            futureDate,
            "Music",
            null,
            null,
            null,
            null,
            null);

    given(ticketmasterClient.fetchUpcomingEvents(any(Instant.class), any(Instant.class), eq(1000)))
        .willReturn(List.of(tmEvent));

    Category category = Category.builder().id(1L).name("Konzert").slug("konzert").build();
    given(categoryRepository.findByNameIgnoreCase("Konzert")).willReturn(Optional.of(category));

    given(locationRepository.findByNameAndCity("Unknown Venue", "Unknown"))
        .willReturn(Optional.empty());
    Location savedLocation =
        Location.builder().id(10L).name("Unknown Venue").city("Unknown").country("CH").build();
    given(locationRepository.save(any(Location.class))).willReturn(savedLocation);

    given(eventRepository.findByExternalId("tm-5")).willReturn(Optional.empty());
    given(eventRepository.save(any(Event.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    int count = integrationService.syncUpcomingEvents();

    assertThat(count).isEqualTo(1);
    then(locationRepository).should().save(any(Location.class));
  }

  @Test
  void syncUpcomingEvents_mapsUnknownSegmentToSonstiges() {
    given(eventRepository.deleteByStartDateBefore(any(LocalDateTime.class))).willReturn(0);

    LocalDateTime futureDate = LocalDateTime.now().plusDays(5);
    TicketmasterEvent tmEvent =
        buildTmEvent("tm-unk", "Unknown Show", futureDate, "NewSegment", "Club", "Basel");

    given(ticketmasterClient.fetchUpcomingEvents(any(Instant.class), any(Instant.class), eq(1000)))
        .willReturn(List.of(tmEvent));

    Category sonstiges = Category.builder().id(7L).name("Sonstiges").slug("sonstiges").build();
    given(categoryRepository.findByNameIgnoreCase("Sonstiges")).willReturn(Optional.of(sonstiges));

    Location location = Location.builder().id(1L).name("Club").city("Basel").country("CH").build();
    given(locationRepository.findByNameAndCity("Club", "Basel")).willReturn(Optional.of(location));

    given(eventRepository.findByExternalId("tm-unk")).willReturn(Optional.empty());
    given(eventRepository.save(any(Event.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    int count = integrationService.syncUpcomingEvents();

    assertThat(count).isEqualTo(1);
    then(categoryRepository).should(never()).save(any(Category.class));
  }

  @Test
  void syncUpcomingEvents_handlesEmptyFetchResult() {
    given(eventRepository.deleteByStartDateBefore(any(LocalDateTime.class))).willReturn(0);
    given(ticketmasterClient.fetchUpcomingEvents(any(Instant.class), any(Instant.class), anyInt()))
        .willReturn(List.of());

    int count = integrationService.syncUpcomingEvents();

    assertThat(count).isZero();
    then(eventRepository).should(never()).save(any(Event.class));
  }

  @Test
  void syncUpcomingEvents_continuesAfterIndividualEventFailure() {
    given(eventRepository.deleteByStartDateBefore(any(LocalDateTime.class))).willReturn(0);

    LocalDateTime futureDate = LocalDateTime.now().plusDays(5);
    TicketmasterEvent failEvent =
        buildTmEvent("tm-fail", "Failing Event", futureDate, "Music", "Arena", "Zurich");
    TicketmasterEvent successEvent =
        buildTmEvent("tm-ok", "Good Event", futureDate, "Music", "Arena", "Zurich");

    given(ticketmasterClient.fetchUpcomingEvents(any(Instant.class), any(Instant.class), eq(1000)))
        .willReturn(List.of(failEvent, successEvent));

    Category category = Category.builder().id(1L).name("Konzert").slug("konzert").build();
    given(categoryRepository.findByNameIgnoreCase("Konzert")).willReturn(Optional.of(category));

    Location location =
        Location.builder().id(1L).name("Arena").city("Zurich").country("CH").build();
    given(locationRepository.findByNameAndCity("Arena", "Zurich"))
        .willReturn(Optional.of(location));

    given(eventRepository.findByExternalId("tm-fail"))
        .willThrow(new RuntimeException("DB connection lost"));
    given(eventRepository.findByExternalId("tm-ok")).willReturn(Optional.empty());
    given(eventRepository.save(any(Event.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    int count = integrationService.syncUpcomingEvents();

    assertThat(count).isEqualTo(1);
  }
}
