package ch.loete.backend.web.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.domain.service.FavoriteService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.testconfig.TestSecurityConfig;
import ch.loete.backend.web.dto.response.FavoriteResponse;
import ch.loete.backend.web.dto.response.MigrateFavoritesResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FavoriteController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class FavoriteControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FavoriteService favoriteService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private UserRepository userRepository;

  private static final String USER_ID = "usr00001";
  private static final String USER_EMAIL = TestSecurityConfig.TEST_USER_EMAIL;

  @BeforeEach
  void setUp() {
    User u = User.builder().id(USER_ID).email(USER_EMAIL).username("testuser").build();
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(u));
  }

  @Test
  void getFavorites_returnsList() throws Exception {
    LocalDateTime now = LocalDateTime.of(2026, 6, 15, 20, 0);
    FavoriteResponse fav =
        new FavoriteResponse(
            "fav12345",
            "evt12345",
            "Rock Concert",
            "https://img.url/pic.jpg",
            now,
            "Konzert",
            "Hallenstadion",
            "Zurich",
            Instant.now());

    when(favoriteService.getFavorites(USER_ID)).thenReturn(List.of(fav));

    mockMvc
        .perform(get("/favorites"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value("fav12345"))
        .andExpect(jsonPath("$[0].eventId").value("evt12345"))
        .andExpect(jsonPath("$[0].name").value("Rock Concert"));
  }

  @Test
  void getFavoriteIds_returnsIdList() throws Exception {
    when(favoriteService.getFavoriteEventIds(USER_ID)).thenReturn(List.of("evt1", "evt2"));

    mockMvc
        .perform(get("/favorites/ids"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0]").value("evt1"))
        .andExpect(jsonPath("$[1]").value("evt2"));
  }

  @Test
  void addFavorite_returns200() throws Exception {
    LocalDateTime now = LocalDateTime.of(2026, 6, 15, 20, 0);
    FavoriteResponse fav =
        new FavoriteResponse(
            "fav12345",
            "evt12345",
            "Rock Concert",
            null,
            now,
            "Konzert",
            null,
            null,
            Instant.now());

    when(favoriteService.addFavorite(USER_ID, "evt12345")).thenReturn(fav);

    mockMvc
        .perform(post("/favorites/evt12345"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("fav12345"))
        .andExpect(jsonPath("$.eventId").value("evt12345"));
  }

  @Test
  void removeFavorite_returns204() throws Exception {
    doNothing().when(favoriteService).removeFavorite(USER_ID, "evt12345");

    mockMvc.perform(delete("/favorites/evt12345")).andExpect(status().isNoContent());
  }

  @Test
  void removeFavorite_returns404WhenNotFound() throws Exception {
    doThrow(new ResourceNotFoundException("Favorite", "evt12345"))
        .when(favoriteService)
        .removeFavorite(USER_ID, "evt12345");

    mockMvc.perform(delete("/favorites/evt12345")).andExpect(status().isNotFound());
  }

  @Test
  void migrateFavorites_returnsMigratedAndSkipped() throws Exception {
    MigrateFavoritesResponse response =
        new MigrateFavoritesResponse(List.of("evt1"), List.of("evt2"));
    when(favoriteService.migrateFavorites(USER_ID, List.of("evt1", "evt2"))).thenReturn(response);

    mockMvc
        .perform(
            post("/favorites/migrate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventIds\":[\"evt1\",\"evt2\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.migrated[0]").value("evt1"))
        .andExpect(jsonPath("$.skipped[0]").value("evt2"));
  }
}
