package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.FavoriteRepository;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.response.EventDetailResponse;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
