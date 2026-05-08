package ch.loete.backend.domain.service;

import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.web.dto.response.CategoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service für Kategorie-Operationen.
 *
 * <p>Stellt Methoden zum Abrufen aller Event-Kategorien bereit, alphabetisch sortiert nach Name.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

  /** Repository für den Zugriff auf Kategorie-Daten. */
  private final CategoryRepository categoryRepository;

  /**
   * Gibt alle Kategorien alphabetisch sortiert als Response-DTOs zurück.
   *
   * @return Liste aller Kategorien
   */
  @Transactional(readOnly = true)
  public List<CategoryResponse> getCategories() {
    return categoryRepository.findAllByOrderByNameAsc().stream()
        .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getSlug()))
        .toList();
  }
}
