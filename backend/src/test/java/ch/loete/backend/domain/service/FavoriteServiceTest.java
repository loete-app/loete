package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Favorite;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.FavoriteRepository;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.response.FavoriteResponse;
import ch.loete.backend.web.dto.response.MigrateFavoritesResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

  @Mock private FavoriteRepository favoriteRepository;

  @Mock private EventRepository eventRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private FavoriteService favoriteService;

  private static final String USER_ID = "usr00001";

  private User buildUser() {
    return User.builder().id(USER_ID).email("test@example.com").username("testuser").build();
  }

  private Event buildEvent() {
    return Event.builder()
        .id("evt00001")
        .name("Rock Concert")
        .imageUrl("https://img.example.com/rock.jpg")
        .startDate(LocalDateTime.of(2026, 7, 15, 20, 0))
        .category(Category.builder().id(1L).name("Music").slug("music").build())
        .location(
            Location.builder().id(1L).name("Hallenstadion").city("Zurich").country("CH").build())
        .build();
  }

  @Test
  void getFavorites_returnsMappedFavorites() {
    Event event = buildEvent();
    User user = buildUser();
    Instant createdAt = Instant.parse("2026-05-01T10:00:00Z");
    Favorite favorite =
        Favorite.builder().id("fav00001").user(user).event(event).createdAt(createdAt).build();

    given(favoriteRepository.findByUser_IdOrderByCreatedAtDesc(USER_ID))
        .willReturn(List.of(favorite));

    List<FavoriteResponse> result = favoriteService.getFavorites(USER_ID);

    assertThat(result).hasSize(1);
    FavoriteResponse response = result.get(0);
    assertThat(response.id()).isEqualTo("fav00001");
    assertThat(response.eventId()).isEqualTo("evt00001");
    assertThat(response.name()).isEqualTo("Rock Concert");
    assertThat(response.imageUrl()).isEqualTo("https://img.example.com/rock.jpg");
    assertThat(response.startDate()).isEqualTo(LocalDateTime.of(2026, 7, 15, 20, 0));
    assertThat(response.categoryName()).isEqualTo("Music");
    assertThat(response.locationName()).isEqualTo("Hallenstadion");
    assertThat(response.city()).isEqualTo("Zurich");
    assertThat(response.createdAt()).isEqualTo(createdAt);
  }

  @Test
  void getFavoriteEventIds_returnsIdsFromRepository() {
    given(favoriteRepository.findEventIdsByUserId(USER_ID))
        .willReturn(Set.of("evt00001", "evt00002"));

    List<String> result = favoriteService.getFavoriteEventIds(USER_ID);

    assertThat(result).containsExactlyInAnyOrder("evt00001", "evt00002");
  }

  @Test
  void addFavorite_createsAndReturnsNewFavorite() {
    Event event = buildEvent();
    User user = buildUser();
    Instant createdAt = Instant.parse("2026-05-06T12:00:00Z");
    Favorite savedFavorite =
        Favorite.builder().id("fav00002").user(user).event(event).createdAt(createdAt).build();

    given(favoriteRepository.findByUser_IdAndEvent_Id(USER_ID, "evt00001"))
        .willReturn(Optional.empty());
    given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
    given(eventRepository.findById("evt00001")).willReturn(Optional.of(event));
    given(favoriteRepository.save(any(Favorite.class))).willReturn(savedFavorite);

    FavoriteResponse result = favoriteService.addFavorite(USER_ID, "evt00001");

    assertThat(result.id()).isEqualTo("fav00002");
    assertThat(result.eventId()).isEqualTo("evt00001");
    assertThat(result.name()).isEqualTo("Rock Concert");
    assertThat(result.createdAt()).isEqualTo(createdAt);
  }

  @Test
  void addFavorite_returnsExistingWhenAlreadyFavorited() {
    Event event = buildEvent();
    User user = buildUser();
    Instant createdAt = Instant.parse("2026-05-01T10:00:00Z");
    Favorite existing =
        Favorite.builder().id("fav00001").user(user).event(event).createdAt(createdAt).build();

    given(favoriteRepository.findByUser_IdAndEvent_Id(USER_ID, "evt00001"))
        .willReturn(Optional.of(existing));

    FavoriteResponse result = favoriteService.addFavorite(USER_ID, "evt00001");

    assertThat(result.id()).isEqualTo("fav00001");
    then(favoriteRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  void addFavorite_throwsResourceNotFoundExceptionWhenEventNotFound() {
    User user = buildUser();

    given(favoriteRepository.findByUser_IdAndEvent_Id(USER_ID, "evt00001"))
        .willReturn(Optional.empty());
    given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
    given(eventRepository.findById("evt00001")).willReturn(Optional.empty());

    assertThatThrownBy(() -> favoriteService.addFavorite(USER_ID, "evt00001"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Event")
        .hasMessageContaining("evt00001");
  }

  @Test
  void removeFavorite_deletesFavorite() {
    Event event = buildEvent();
    User user = buildUser();
    Favorite favorite = Favorite.builder().id("fav00001").user(user).event(event).build();

    given(favoriteRepository.findByUser_IdAndEvent_Id(USER_ID, "evt00001"))
        .willReturn(Optional.of(favorite));

    favoriteService.removeFavorite(USER_ID, "evt00001");

    then(favoriteRepository).should().delete(favorite);
  }

  @Test
  void removeFavorite_throwsResourceNotFoundExceptionWhenNotFound() {
    given(favoriteRepository.findByUser_IdAndEvent_Id(USER_ID, "evt00001"))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> favoriteService.removeFavorite(USER_ID, "evt00001"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Favorite");
  }

  @Test
  void migrateFavorites_migratesNewAndSkipsExisting() {
    User user = buildUser();
    Event event1 = buildEvent();
    Event event2 =
        Event.builder()
            .id("evt00002")
            .name("Jazz Night")
            .startDate(LocalDateTime.of(2026, 8, 1, 21, 0))
            .build();

    given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
    given(favoriteRepository.existsByUser_IdAndEvent_Id(USER_ID, "evt00001")).willReturn(true);
    given(favoriteRepository.existsByUser_IdAndEvent_Id(USER_ID, "evt00002")).willReturn(false);
    given(eventRepository.findById("evt00002")).willReturn(Optional.of(event2));
    given(favoriteRepository.save(any(Favorite.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    MigrateFavoritesResponse result =
        favoriteService.migrateFavorites(USER_ID, List.of("evt00001", "evt00002"));

    assertThat(result.migrated()).containsExactly("evt00002");
    assertThat(result.skipped()).containsExactly("evt00001");
  }

  @Test
  void migrateFavorites_skipsNonExistentEvents() {
    User user = buildUser();

    given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
    given(favoriteRepository.existsByUser_IdAndEvent_Id(USER_ID, "evt99999")).willReturn(false);
    given(eventRepository.findById("evt99999")).willReturn(Optional.empty());

    MigrateFavoritesResponse result =
        favoriteService.migrateFavorites(USER_ID, List.of("evt99999"));

    assertThat(result.migrated()).isEmpty();
    assertThat(result.skipped()).containsExactly("evt99999");
  }
}
