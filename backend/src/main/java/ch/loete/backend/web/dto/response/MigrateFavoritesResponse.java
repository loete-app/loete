package ch.loete.backend.web.dto.response;

import java.util.List;

/**
 * Response-DTO für die Favoriten-Migration.
 *
 * <p>Gibt Auskunft darueber, welche Favoriten erfolgreich migriert und welche übersprungen wurden
 * (bereits vorhanden oder Event nicht gefunden).
 *
 * @param migrated die erfolgreich migrierten Event-IDs
 * @param skipped die übersprungenen Event-IDs
 */
public record MigrateFavoritesResponse(List<String> migrated, List<String> skipped) {}
