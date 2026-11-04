package ch.loete.backend.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.service.VibeSearchService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.testconfig.TestSecurityConfig;
import ch.loete.backend.web.dto.request.VibeSearchRequest;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.VibeSearchResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VibeSearchController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class VibeSearchControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private VibeSearchService vibeSearchService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private UserRepository userRepository;

  @Test
  void vibeSearch_returnsResults() throws Exception {
    EventResponse event =
        new EventResponse(
            "ev1",
            "Jazz Night",
            "https://img.url/jazz.jpg",
            LocalDateTime.of(2026, 7, 15, 20, 0),
            "Konzert",
            "Hallenstadion",
            "Zurich");
    VibeSearchResponse response = new VibeSearchResponse(List.of(event), false);

    when(vibeSearchService.search(any(VibeSearchRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/search/vibe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"query": "gemütlicher Jazz-Abend"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results").isArray())
        .andExpect(jsonPath("$.results[0].id").value("ev1"))
        .andExpect(jsonPath("$.results[0].name").value("Jazz Night"))
        .andExpect(jsonPath("$.fallback").value(false));
  }

  @Test
  void vibeSearch_returnsFallbackResponse() throws Exception {
    VibeSearchResponse response = new VibeSearchResponse(List.of(), true);

    when(vibeSearchService.search(any(VibeSearchRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/search/vibe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"query": "some vibe query"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results").isArray())
        .andExpect(jsonPath("$.fallback").value(true));
  }

  @Test
  void vibeSearch_returns400WhenQueryTooShort() throws Exception {
    mockMvc
        .perform(
            post("/search/vibe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"query": "ab"}
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void vibeSearch_returns400WhenQueryMissing() throws Exception {
    mockMvc
        .perform(post("/search/vibe").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }
}
