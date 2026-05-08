package ch.loete.backend.web.dto.response;

/**
 * Response-DTO für eine Event-Kategorie.
 *
 * @param id die Kategorie-ID
 * @param name der Anzeigename der Kategorie
 * @param slug der URL-freundliche Slug
 */
public record CategoryResponse(Long id, String name, String slug) {}
