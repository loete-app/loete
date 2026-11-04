package ch.loete.backend.process.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Favorite;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.testconfig.TestcontainersConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class FavoriteRepositoryTest {

  @Autowired private FavoriteRepository favoriteRepository;

  @Autowired private TestEntityManager entityManager;

  private Event event1;
  private Event event2;
  private User testUser;

  @BeforeEach
  void setUp() {
    Category category =
        entityManager
            .getEntityManager()
            .createQuery("SELECT c FROM Category c WHERE c.slug = 'konzert'", Category.class)
            .getSingleResult();

    Location location =
        Location.builder().name("Hallenstadion").city("Zurich").country("CH").build();
    location = entityManager.persist(location);

    testUser =
        User.builder()
            .email("repo-test@example.com")
            .username("repotest")
            .passwordHash("$2a$10$hashed")
            .build();
    testUser = entityManager.persist(testUser);

    event1 =
        Event.builder()
            .externalId("TM-E001")
            .name("Event One")
            .startDate(LocalDateTime.of(2026, 8, 15, 20, 0))
            .category(category)
            .location(location)
            .build();
    event1 = entityManager.persist(event1);

    event2 =
        Event.builder()
            .externalId("TM-E002")
            .name("Event Two")
            .startDate(LocalDateTime.of(2026, 9, 1, 19, 0))
            .category(category)
            .location(location)
            .build();
    event2 = entityManager.persist(event2);
    entityManager.flush();
  }

  @Test
  void findByUserIdOrderByCreatedAtDesc_returnsFavorites() {
    Favorite fav1 = Favorite.builder().user(testUser).event(event1).build();
    entityManager.persist(fav1);
    Favorite fav2 = Favorite.builder().user(testUser).event(event2).build();
    entityManager.persist(fav2);
    entityManager.flush();

    List<Favorite> favorites =
        favoriteRepository.findByUser_IdOrderByCreatedAtDesc(testUser.getId());

    assertThat(favorites).hasSize(2);
  }

  @Test
  void existsByUserIdAndEventId_returnsTrueWhenExists() {
    Favorite fav = Favorite.builder().user(testUser).event(event1).build();
    entityManager.persist(fav);
    entityManager.flush();

    boolean exists =
        favoriteRepository.existsByUser_IdAndEvent_Id(testUser.getId(), event1.getId());

    assertThat(exists).isTrue();
  }

  @Test
  void existsByUserIdAndEventId_returnsFalseWhenNotExists() {
    boolean exists =
        favoriteRepository.existsByUser_IdAndEvent_Id(testUser.getId(), event1.getId());

    assertThat(exists).isFalse();
  }

  @Test
  void findByUserIdAndEventId_returnsMatchingFavorite() {
    Favorite fav = Favorite.builder().user(testUser).event(event1).build();
    entityManager.persist(fav);
    entityManager.flush();

    Optional<Favorite> result =
        favoriteRepository.findByUser_IdAndEvent_Id(testUser.getId(), event1.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getEvent().getId()).isEqualTo(event1.getId());
  }

  @Test
  void findEventIdsByUserId_returnsSetOfEventIds() {
    Favorite fav1 = Favorite.builder().user(testUser).event(event1).build();
    entityManager.persist(fav1);
    Favorite fav2 = Favorite.builder().user(testUser).event(event2).build();
    entityManager.persist(fav2);
    entityManager.flush();

    Set<String> eventIds = favoriteRepository.findEventIdsByUserId(testUser.getId());

    assertThat(eventIds).containsExactlyInAnyOrder(event1.getId(), event2.getId());
  }

  @Test
  void uniqueConstraint_preventsDuplicateUserIdEventId() {
    Favorite fav1 = Favorite.builder().user(testUser).event(event1).build();
    entityManager.persist(fav1);
    entityManager.flush();

    Favorite fav2 = Favorite.builder().user(testUser).event(event1).build();

    assertThatThrownBy(
            () -> {
              entityManager.persist(fav2);
              entityManager.flush();
            })
        .isInstanceOf(Exception.class);
  }
}
