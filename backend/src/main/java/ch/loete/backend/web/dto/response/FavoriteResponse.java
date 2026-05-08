package ch.loete.backend.web.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Response-DTO für einen Benutzer-Favoriten.
 *
 * <p>Enthält die Favoriten-ID sowie die wichtigsten Event-Daten für die Favoritenansicht.
 *
 * @param id die Favoriten-ID
 * @param eventId die Event-ID
 * @param name der Event-Name
 * @param imageUrl die URL des Vorschaubildes
 * @param startDate das Startdatum und die Startzeit
 * @param categoryName der Kategoriename
 * @param locationName der Name des Veranstaltungsortes
 * @param city die Stadt
 * @param createdAt der Zeitpunkt der Favoritenerstellung
 */
public record FavoriteResponse(
    String id,
    String eventId,
    String name,
    String imageUrl,
    LocalDateTime startDate,
    String categoryName,
    String locationName,
    String city,
    Instant createdAt) {}
