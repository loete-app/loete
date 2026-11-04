package ch.loete.backend.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.service.CategoryService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.response.CategoryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CategoryService categoryService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private UserRepository userRepository;

  @Test
  void getCategories_returnsList() throws Exception {
    List<CategoryResponse> categories =
        List.of(
            new CategoryResponse(1L, "Comedy", "comedy"),
            new CategoryResponse(2L, "Konzert", "konzert"),
            new CategoryResponse(3L, "Sport", "sport"));

    when(categoryService.getCategories()).thenReturn(categories);

    mockMvc
        .perform(get("/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Comedy"))
        .andExpect(jsonPath("$[0].slug").value("comedy"))
        .andExpect(jsonPath("$[2].name").value("Sport"));
  }
}
