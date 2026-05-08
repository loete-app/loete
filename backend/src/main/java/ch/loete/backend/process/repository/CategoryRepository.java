package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA-Repository für den Datenbankzugriff auf {@link Category}-Entitäten. */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /**
   * Findet eine Kategorie anhand ihres URL-Slugs.
   *
   * @param slug der URL-Slug
   * @return die Kategorie, falls vorhanden
   */
  Optional<Category> findBySlug(String slug);

  /**
   * Findet eine Kategorie anhand ihres Namens (case-insensitive).
   *
   * @param name der Kategoriename
   * @return die Kategorie, falls vorhanden
   */
  Optional<Category> findByNameIgnoreCase(String name);

  /**
   * Gibt alle Kategorien alphabetisch sortiert nach Name zurück.
   *
   * @return die sortierte Kategorie-Liste
   */
  List<Category> findAllByOrderByNameAsc();
}
