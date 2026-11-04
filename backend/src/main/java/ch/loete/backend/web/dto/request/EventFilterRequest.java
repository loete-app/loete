package ch.loete.backend.web.dto.request;

import java.time.LocalDateTime;

/**
 * Request-DTO für die Filterung und Paginierung von Events.
 *
 * <p>Enthält optionale Filter (Kategorie, Stadt, Datumsbereich, Textsuche) sowie
 * Paginierungsparameter. Der Compact-Constructor normalisiert ungültige Werte (z.B. negative
 * Seitennummern).
 *
 * @param categoryId optionale Kategorie-ID zum Filtern
 * @param city optionaler Stadtname zum Filtern
 * @param dateFrom optionaler frühester Startzeitpunkt
 * @param dateTo optionaler spätester Startzeitpunkt
 * @param search optionaler Suchbegriff für den Event-Namen
 * @param page Seitennummer (Standard: 0)
 * @param size Seitengrösse (Standard: 20, Maximum: 100)
 */
public record EventFilterRequest(
    Long categoryId,
    String city,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    String search,
    Integer page,
    Integer size) {

  /** Compact-Constructor zur Normalisierung der Parameter. */
  public EventFilterRequest {
    if (page == null || page < 0) {
      page = 0;
    }
    if (size == null || size < 1) {
      size = 20;
    }
    if (size > 100) {
      size = 100;
    }
    if (city != null && city.isBlank()) {
      city = null;
    }
    if (search != null && search.isBlank()) {
      search = null;
    }
  }
}
