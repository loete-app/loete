package ch.loete.backend.web.dto.response;

import java.util.List;

/**
 * Generisches Response-DTO für paginierte Ergebnisse.
 *
 * @param <T> der Typ der Elemente in der Ergebnisliste
 * @param content die Elemente der aktuellen Seite
 * @param page die aktuelle Seitennummer (0-basiert)
 * @param size die Seitengrösse
 * @param totalElements die Gesamtanzahl der Elemente
 * @param totalPages die Gesamtanzahl der Seiten
 * @param last ob dies die letzte Seite ist
 */
public record PagedResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {}
