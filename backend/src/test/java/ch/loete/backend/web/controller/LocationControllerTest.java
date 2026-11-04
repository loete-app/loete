package ch.loete.backend.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.service.LocationService;
import ch.loete.backend.process.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LocationService locationService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private UserRepository userRepository;

  @Test
  void getCities_returnsList() throws Exception {
    when(locationService.getCities()).thenReturn(List.of("Basel", "Bern", "Zurich"));

    mockMvc
        .perform(get("/locations/cities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0]").value("Basel"))
        .andExpect(jsonPath("$[1]").value("Bern"))
        .andExpect(jsonPath("$[2]").value("Zurich"));
  }
}
