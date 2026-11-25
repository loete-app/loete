package ch.loete.backend.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.domain.service.EventService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.testconfig.TestSecurityConfig;
import ch.loete.backend.web.dto.request.CreateEventRequest;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.response.EventDetailResponse;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

  private static final String START = "2099-07-01T18:00:00";
  private static final String END = "2099-07-01T23:00:00";
  private static final String TICKET_URL = "https://example.ch/sommerfest";

  private static String createBody(String startDate, String endDate, String ticketUrl) {
    return "{"
        + "\"name\": \"Sommerfest\","
        + "\"description\": \"Grillieren am See\","
        + "\"imageUrl\": \"\","
        + "\"ticketUrl\": \""
        + ticketUrl
        + "\","
        + "\"startDate\": \""
        + startDate
        + "\","
        + "\"endDate\": \""
        + endDate
        + "\","
        + "\"categoryId\": 4,"
        + "\"locationName\": \"Landiwiese\","
        + "\"city\": \"Zürich\""
        + "}";
  }

  @Test
  void createEvent_returns201WithLocationHeader() throws Exception {
    User user =
        User.builder()
            .id("usr00001")
            .email(TestSecurityConfig.TEST_USER_EMAIL)
            .username("tester")
            .build();
    when(userRepository.findByEmail(TestSecurityConfig.TEST_USER_EMAIL))
        .thenReturn(Optional.of(user));

    EventDetailResponse created =
        new EventDetailResponse(
            "new12345",
            "Sommerfest",
            "Grillieren am See",
            null,
            "https://example.ch/sommerfest",
            LocalDateTime.of(2099, 7, 1, 18, 0),
            LocalDateTime.of(2099, 7, 1, 23, 0),
            "Festival",
            "festival",
            "Landiwiese",
            "Zürich",
            "CH",
            null,
            null,
            false);
    when(eventService.createEvent(any(CreateEventRequest.class), eq("usr00001")))
        .thenReturn(created);
    String body = createBody(START, END, TICKET_URL);

    mockMvc
        .perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", containsString("/events/new12345")))
        .andExpect(jsonPath("$.id").value("new12345"))
        .andExpect(jsonPath("$.name").value("Sommerfest"));
  }

  @Test
  void createEvent_returns400WhenRequiredFieldsMissing() throws Exception {
    mockMvc
        .perform(post("/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());

    verify(eventService, never()).createEvent(any(), any());
  }

  @Test
  void createEvent_returns400WhenEndDateBeforeStartDate() throws Exception {
    String body = createBody(START, "2099-07-01T17:00:00", TICKET_URL);

    mockMvc
        .perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("endDate")));

    verify(eventService, never()).createEvent(any(), any());
  }

  @Test
  void createEvent_returns400WhenStartDateInPast() throws Exception {
    String body = createBody("2000-07-01T18:00:00", "2000-07-01T23:00:00", TICKET_URL);

    mockMvc
        .perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createEvent_returns400ForInvalidUrl() throws Exception {
    String body = createBody(START, END, "javascript:alert(1)");

    mockMvc
        .perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createEvent_returns400ForMalformedJson() throws Exception {
    mockMvc
        .perform(
            post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"startDate\": \"not-a-date\"}"))
        .andExpect(status().isBadRequest());
  }
}
