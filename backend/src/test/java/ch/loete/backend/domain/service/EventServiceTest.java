package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock private EventRepository eventRepository;

  @Mock private FavoriteRepository favoriteRepository;

  @Mock private CategoryRepository categoryRepository;

  @Mock private LocationRepository locationRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private EventService eventService;

  @Test
  void getEvents_returnsPagedResponseWithCorrectMapping() {
    Category category = Category.builder().id(1L).name("Music").slug("music").build();
    Location location =
        Location.builder()
            .id(1L)
            .name("Hallenstadion")
            .city("Zurich")
            .country("CH")
            .latitude(47.41)
            .longitude(8.55)
            .build();
    LocalDateTime startDate = LocalDateTime.of(2026, 7, 15, 20, 0);
    Event event =
        Event.builder()
            .id("abc12345")
            .name("Rock Concert")
            .imageUrl("https://img.example.com/rock.jpg")
            .startDate(startDate)
            .category(category)
            .location(location)
            .build();

    PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("startDate").ascending());
    PageImpl<Event> page = new PageImpl<>(List.of(event), pageRequest, 1);

    given(eventRepository.findAll(ArgumentMatchers.<Specification<Event>>any(), eq(pageRequest)))
        .willReturn(page);

    EventFilterRequest filter = new EventFilterRequest(null, null, null, null, null, 0, 20);
    PagedResponse<EventResponse> result = eventService.getEvents(filter);

    assertThat(result.content()).hasSize(1);
    assertThat(result.page()).isZero();
    assertThat(result.size()).isEqualTo(20);
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.totalPages()).isEqualTo(1);
    assertThat(result.last()).isTrue();

    EventResponse response = result.content().get(0);
    assertThat(response.id()).isEqualTo("abc12345");
    assertThat(response.name()).isEqualTo("Rock Concert");
    assertThat(response.imageUrl()).isEqualTo("https://img.example.com/rock.jpg");
    assertThat(response.startDate()).isEqualTo(startDate);
    assertThat(response.categoryName()).isEqualTo("Music");
    assertThat(response.locationName()).isEqualTo("Hallenstadion");
    assertThat(response.city()).isEqualTo("Zurich");
  }

  @Test
  void getEvents_returnsPagedResponseWhenCategoryAndLocationAreNull() {
    Event event =
        Event.builder()
            .id("xyz98765")
            .name("Mystery Event")
            .imageUrl(null)
            .startDate(LocalDateTime.of(2026, 8, 1, 18, 0))
            .category(null)
            .location(null)
            .build();

    PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("startDate").ascending());
    PageImpl<Event> page = new PageImpl<>(List.of(event), pageRequest, 1);

    given(eventRepository.findAll(ArgumentMatchers.<Specification<Event>>any(), eq(pageRequest)))
        .willReturn(page);

    EventFilterRequest filter = new EventFilterRequest(null, null, null, null, null, 0, 20);
    PagedResponse<EventResponse> result = eventService.getEvents(filter);

    EventResponse response = result.content().get(0);
    assertThat(response.categoryName()).isNull();
    assertThat(response.locationName()).isNull();
    assertThat(response.city()).isNull();
  }

  @Test
  void getEvents_passesFilterSpecificationsCorrectly() {
    LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);
    EventFilterRequest filter = new EventFilterRequest(1L, "Zurich", from, to, "rock", 2, 10);

    PageRequest pageRequest = PageRequest.of(2, 10, Sort.by("startDate").ascending());
    PageImpl<Event> page = new PageImpl<>(List.of(), pageRequest, 0);

    given(eventRepository.findAll(any(Specification.class), eq(pageRequest))).willReturn(page);

    PagedResponse<EventResponse> result = eventService.getEvents(filter);

    assertThat(result.content()).isEmpty();
    assertThat(result.page()).isEqualTo(2);
    assertThat(result.size()).isEqualTo(10);
  }

  @Test
  void getEvent_returnsDetailWithFavoritedTrue() {
    Category category = Category.builder().id(1L).name("Sports").slug("sports").build();
    Location location =
        Location.builder()
            .id(2L)
            .name("Letzigrund")
            .city("Zurich")
            .country("CH")
            .latitude(47.38)
            .longitude(8.50)
            .build();
    Event event =
        Event.builder()
            .id("evt00001")
            .name("Football Match")
            .description("Champions League final")
            .imageUrl("https://img.example.com/football.jpg")
            .ticketUrl("https://tickets.example.com/evt00001")
            .startDate(LocalDateTime.of(2026, 9, 10, 21, 0))
            .endDate(LocalDateTime.of(2026, 9, 10, 23, 0))
            .category(category)
            .location(location)
            .build();

    given(eventRepository.findById("evt00001")).willReturn(Optional.of(event));
    given(favoriteRepository.existsByUser_IdAndEvent_Id("user-1", "evt00001")).willReturn(true);

    EventDetailResponse result = eventService.getEvent("evt00001", "user-1");

    assertThat(result.id()).isEqualTo("evt00001");
    assertThat(result.name()).isEqualTo("Football Match");
    assertThat(result.description()).isEqualTo("Champions League final");
    assertThat(result.imageUrl()).isEqualTo("https://img.example.com/football.jpg");
    assertThat(result.ticketUrl()).isEqualTo("https://tickets.example.com/evt00001");
    assertThat(result.startDate()).isEqualTo(LocalDateTime.of(2026, 9, 10, 21, 0));
    assertThat(result.endDate()).isEqualTo(LocalDateTime.of(2026, 9, 10, 23, 0));
    assertThat(result.categoryName()).isEqualTo("Sports");
    assertThat(result.categorySlug()).isEqualTo("sports");
    assertThat(result.locationName()).isEqualTo("Letzigrund");
    assertThat(result.city()).isEqualTo("Zurich");
    assertThat(result.country()).isEqualTo("CH");
    assertThat(result.latitude()).isEqualTo(47.38);
    assertThat(result.longitude()).isEqualTo(8.50);
    assertThat(result.favorited()).isTrue();
  }

  @Test
  void getEvent_returnsDetailWithFavoritedFalseWhenNoUserId() {
    Event event =
        Event.builder().id("evt00002").name("Jazz Night").category(null).location(null).build();

    given(eventRepository.findById("evt00002")).willReturn(Optional.of(event));

    EventDetailResponse result = eventService.getEvent("evt00002", null);

    assertThat(result.favorited()).isFalse();
    assertThat(result.categoryName()).isNull();
    assertThat(result.locationName()).isNull();
  }

  @Test
  void getEvent_throwsResourceNotFoundExceptionWhenEventNotFound() {
    given(eventRepository.findById("missing")).willReturn(Optional.empty());

    assertThatThrownBy(() -> eventService.getEvent("missing", null))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Event")
        .hasMessageContaining("missing");
  }

  private static CreateEventRequest createRequest() {
    return new CreateEventRequest(
        "  Sommerfest  ",
        "  ",
        "",
        "https://example.ch/sommerfest",
        LocalDateTime.of(2099, 7, 1, 18, 0),
        null,
        4L,
        " Landiwiese ",
        "Zürich");
  }

  @Test
  void createEvent_createsNewLocationAndSetsCreator() {
    User user = User.builder().id("usr00001").email("a@b.ch").username("tester").build();
    Category festival = Category.builder().id(4L).name("Festival").slug("festival").build();
    given(userRepository.findById("usr00001")).willReturn(Optional.of(user));
    given(categoryRepository.findById(4L)).willReturn(Optional.of(festival));
    given(locationRepository.findByNameAndCity("Landiwiese", "Zürich"))
        .willReturn(Optional.empty());
    given(locationRepository.save(any(Location.class)))
        .willAnswer(
            inv -> {
              Location l = inv.getArgument(0);
              l.setId(9L);
              return l;
            });
    given(eventRepository.save(any(Event.class)))
        .willAnswer(
            inv -> {
              Event e = inv.getArgument(0);
              e.setId("new12345");
              return e;
            });

    EventDetailResponse result = eventService.createEvent(createRequest(), "usr00001");

    ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
    verify(eventRepository).save(captor.capture());
    Event saved = captor.getValue();
    assertThat(saved.getName()).isEqualTo("Sommerfest");
    assertThat(saved.getDescription()).isNull();
    assertThat(saved.getImageUrl()).isNull();
    assertThat(saved.getTicketUrl()).isEqualTo("https://example.ch/sommerfest");
    assertThat(saved.getSource()).isEqualTo(EventService.SOURCE_USER);
    assertThat(saved.getCreatedBy()).isEqualTo(user);
    assertThat(saved.getExternalId()).isNull();
    assertThat(saved.getLocation().getName()).isEqualTo("Landiwiese");
    assertThat(saved.getLocation().getCountry()).isEqualTo("CH");

    assertThat(result.id()).isEqualTo("new12345");
    assertThat(result.categoryName()).isEqualTo("Festival");
    assertThat(result.locationName()).isEqualTo("Landiwiese");
    assertThat(result.city()).isEqualTo("Zürich");
    assertThat(result.favorited()).isFalse();
  }

  @Test
  void createEvent_reusesExistingLocation() {
    User user = User.builder().id("usr00001").build();
    Category festival = Category.builder().id(4L).name("Festival").slug("festival").build();
    Location existing = Location.builder().id(3L).name("Landiwiese").city("Zürich").build();
    given(userRepository.findById("usr00001")).willReturn(Optional.of(user));
    given(categoryRepository.findById(4L)).willReturn(Optional.of(festival));
    given(locationRepository.findByNameAndCity("Landiwiese", "Zürich"))
        .willReturn(Optional.of(existing));
    given(eventRepository.save(any(Event.class))).willAnswer(inv -> inv.getArgument(0));

    eventService.createEvent(createRequest(), "usr00001");

    verify(locationRepository, never()).save(any());
    ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
    verify(eventRepository).save(captor.capture());
    assertThat(captor.getValue().getLocation()).isSameAs(existing);
  }

  @Test
  void createEvent_throwsWhenCategoryNotFound() {
    given(userRepository.findById("usr00001"))
        .willReturn(Optional.of(User.builder().id("usr00001").build()));
    given(categoryRepository.findById(4L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> eventService.createEvent(createRequest(), "usr00001"))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(eventRepository, never()).save(any());
  }
}
