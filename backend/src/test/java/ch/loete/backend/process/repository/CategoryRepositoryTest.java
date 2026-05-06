package ch.loete.backend.process.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.testconfig.TestcontainersConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class CategoryRepositoryTest {

  @Autowired private CategoryRepository categoryRepository;

  @Test
  void findBySlug_returnsCategory() {
    Optional<Category> result = categoryRepository.findBySlug("konzert");

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Konzert");
  }

  @Test
  void findBySlug_returnsEmptyForUnknownSlug() {
    Optional<Category> result = categoryRepository.findBySlug("nonexistent");

    assertThat(result).isEmpty();
  }

  @Test
  void findByNameIgnoreCase_returnsCategoryRegardlessOfCase() {
    Optional<Category> result = categoryRepository.findByNameIgnoreCase("KONZERT");

    assertThat(result).isPresent();
    assertThat(result.get().getSlug()).isEqualTo("konzert");
  }

  @Test
  void findByNameIgnoreCase_returnsEmptyForUnknownName() {
    Optional<Category> result = categoryRepository.findByNameIgnoreCase("Unknown");

    assertThat(result).isEmpty();
  }

  @Test
  void findAllByOrderByNameAsc_returnsAllPreSeededCategoriesSorted() {
    List<Category> categories = categoryRepository.findAllByOrderByNameAsc();

    assertThat(categories).hasSize(7);
    assertThat(categories.getFirst().getName()).isEqualTo("Comedy");
    assertThat(categories.getLast().getName()).isEqualTo("Theater");

    // Verify alphabetical ordering
    List<String> names = categories.stream().map(Category::getName).toList();
    assertThat(names).isSorted();
  }
}
