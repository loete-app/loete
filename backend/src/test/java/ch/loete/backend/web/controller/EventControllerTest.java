package ch.loete.backend.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.service.EventService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.testconfig.TestSecurityConfig;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.response.EventDetailResponse;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class EventControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EventService eventService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private UserRepository userRepository;

  @Test
  void getEvents_returnsPagedEvents() throws Exception {
    LocalDateTime now = LocalDateTime.of(2026, 6, 15, 20, 0);
    EventResponse event =
        new EventResponse(
            "abc12345",
            "Rock Concert",
            "https://img.url/pic.jpg",
            now,
            "Konzert",
            "Hallenstadion",
            "Zurich");
    PagedResponse<EventResponse> response = new PagedResponse<>(List.of(event), 0, 20, 1, 1, true);

    when(eventService.getEvents(any(EventFilterRequest.class))).thenReturn(response);

    mockMvc
        .perform(get("/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value("abc12345"))
        .andExpect(jsonPath("$.content[0].name").value("Rock Concert"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  void getEvents_usesDefaultPageAndSize() throws Exception {
    PagedResponse<EventResponse> response = new PagedResponse<>(List.of(), 0, 20, 0, 0, true);

    when(eventService.getEvents(any(EventFilterRequest.class))).thenReturn(response);

    mockMvc
        .perform(get("/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20));
  }

  @Test
  void getEvent_returnsEventDetailWithoutExtraAuth() throws Exception {
    LocalDateTime start = LocalDateTime.of(2026, 6, 15, 20, 0);
    LocalDateTime end = LocalDateTime.of(2026, 6, 15, 23, 0);
    EventDetailResponse detail =
        new EventDetailResponse(
            "abc12345",
            "Rock Concert",
            "A great concert",
            "https://img.url/pic.jpg",
            "https://tickets.url",
            start,
            end,
            "Konzert",
            "konzert",
            "Hallenstadion",
            "Zurich",
            "CH",
            47.41,
            8.55,
            false);

    when(eventService.getEvent(eq("abc12345"), eq(null))).thenReturn(detail);

    mockMvc
        .perform(get("/events/abc12345"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("abc12345"))
        .andExpect(jsonPath("$.name").value("Rock Concert"))
        .andExpect(jsonPath("$.description").value("A great concert"))
        .andExpect(jsonPath("$.categoryName").value("Konzert"))
        .andExpect(jsonPath("$.favorited").value(false));
  }

  @Test
  void getEvent_returns404WhenNotFound() throws Exception {
    when(eventService.getEvent(eq("nonexist"), eq(null)))
        .thenThrow(new ResourceNotFoundException("Event", "nonexist"));

    mockMvc.perform(get("/events/nonexist")).andExpect(status().isNotFound());
  }
}
