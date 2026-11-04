package ch.loete.backend.web.dto.response;

import java.time.LocalDateTime;

/**
 * Response-DTO für die Event-Listenansicht.
 *
 * <p>Enthält die wichtigsten Informationen eines Events für Kartenansichten und Listeneintraege.
 *
 * @param id die Event-ID
 * @param name der Event-Name
 * @param imageUrl die URL des Vorschaubildes
 * @param startDate das Startdatum und die Startzeit
 * @param categoryName der Kategoriename
 * @param locationName der Name des Veranstaltungsortes
 * @param city die Stadt
 */
public record EventResponse(
    String id,
    String name,
    String imageUrl,
    LocalDateTime startDate,
    String categoryName,
    String locationName,
    String city) {}
