package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.web.dto.response.CategoryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private CategoryService categoryService;

  @Test
  void getCategories_returnsAllCategoriesMapped() {
    Category music = Category.builder().id(1L).name("Music").slug("music").build();
    Category sports = Category.builder().id(2L).name("Sports").slug("sports").build();

    given(categoryRepository.findAllByOrderByNameAsc()).willReturn(List.of(music, sports));

    List<CategoryResponse> result = categoryService.getCategories();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo(1L);
    assertThat(result.get(0).name()).isEqualTo("Music");
    assertThat(result.get(0).slug()).isEqualTo("music");
    assertThat(result.get(1).id()).isEqualTo(2L);
    assertThat(result.get(1).name()).isEqualTo("Sports");
    assertThat(result.get(1).slug()).isEqualTo("sports");
  }

  @Test
  void getCategories_returnsEmptyListWhenNoCategories() {
    given(categoryRepository.findAllByOrderByNameAsc()).willReturn(List.of());

    List<CategoryResponse> result = categoryService.getCategories();

    assertThat(result).isEmpty();
  }
}
