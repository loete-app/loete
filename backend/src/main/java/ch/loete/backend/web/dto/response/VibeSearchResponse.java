package ch.loete.backend.web.dto.response;

import java.util.List;

/**
 * Response-DTO für die Vibe-Suche.
 *
 * @param results die Suchergebnisse als Event-Listeneintraege
 * @param fallback ob nur Keyword-Ergebnisse verwendet wurden (Fallback)
 */
public record VibeSearchResponse(List<EventResponse> results, boolean fallback) {}
