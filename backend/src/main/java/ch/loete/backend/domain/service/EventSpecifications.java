package ch.loete.backend.domain.service;

import ch.loete.backend.domain.model.Event;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA-Specifications für die dynamische Event-Filterung.
 *
 * <p>Stellt statische Factory-Methoden bereit, die JPA-Specifications für verschiedene
 * Filterkriterien erzeugen. Gibt {@code null} zurück, wenn der Filterwert leer ist (Filter wird
 * dann übersprungen).
 */
final class EventSpecifications {

  /** Privater Konstruktor verhindert Instanziierung. */
  private EventSpecifications() {}

  /**
   * Filtert Events nach Kategorie-ID.
   *
   * @param categoryId die Kategorie-ID (oder {@code null} für keinen Filter)
   * @return die Specification oder {@code null}
   */
  static Specification<Event> hasCategory(Long categoryId) {
    if (categoryId == null) {
      return null;
    }
    return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
  }

  /**
   * Filtert Events nach Stadt (case-insensitive).
   *
   * @param city der Stadtname (oder {@code null}/leer für keinen Filter)
   * @return die Specification oder {@code null}
   */
  static Specification<Event> inCity(String city) {
    if (city == null || city.isBlank()) {
      return null;
    }
    return (root, query, cb) ->
        cb.equal(cb.lower(root.get("location").get("city")), city.toLowerCase());
  }

  /**
   * Filtert Events, die nach dem angegebenen Zeitpunkt beginnen.
   *
   * @param dateFrom der früheste Startzeitpunkt (oder {@code null} für keinen Filter)
   * @return die Specification oder {@code null}
   */
  static Specification<Event> startsAfter(LocalDateTime dateFrom) {
    if (dateFrom == null) {
      return null;
    }
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), dateFrom);
  }

  /**
   * Filtert Events, die vor dem angegebenen Zeitpunkt beginnen.
   *
   * @param dateTo der späteste Startzeitpunkt (oder {@code null} für keinen Filter)
   * @return die Specification oder {@code null}
   */
  static Specification<Event> startsBefore(LocalDateTime dateTo) {
    if (dateTo == null) {
      return null;
    }
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), dateTo);
  }

  /**
   * Filtert Events, deren Name den Suchbegriff enthält (case-insensitive).
   *
   * @param search der Suchbegriff (oder {@code null}/leer für keinen Filter)
   * @return die Specification oder {@code null}
   */
  static Specification<Event> nameContains(String search) {
    if (search == null || search.isBlank()) {
      return null;
    }
    String pattern = "%" + search.toLowerCase() + "%";
    return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
  }
}
