package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.CategoryService;
import ch.loete.backend.web.dto.response.CategoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für Kategorie-Endpunkte.
 *
 * <p>Stellt einen öffentlichen GET-Endpunkt für das Abrufen aller Event-Kategorien bereit.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

  /** Service für Kategorie-Operationen. */
  private final CategoryService categoryService;

  /**
   * Gibt alle Event-Kategorien alphabetisch sortiert zurück.
   *
   * @return die Kategorie-Liste
   */
  @GetMapping
  public ResponseEntity<List<CategoryResponse>> getCategories() {
    return ResponseEntity.ok(categoryService.getCategories());
  }
}
