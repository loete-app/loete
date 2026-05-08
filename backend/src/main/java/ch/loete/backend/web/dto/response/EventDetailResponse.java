package ch.loete.backend.web.dto.response;

import java.time.LocalDateTime;

/**
 * Response-DTO für die Event-Detailansicht.
 *
 * <p>Enthält alle Informationen eines Events inklusive Beschreibung, Ticket-URL, Geokoordinaten und
 * Favoritenstatus.
 *
 * @param id die Event-ID
 * @param name der Event-Name
 * @param description die Beschreibung
 * @param imageUrl die URL des Vorschaubildes
 * @param ticketUrl die URL zur Ticketverkaufsseite
 * @param startDate das Startdatum und die Startzeit
 * @param endDate das Enddatum und die Endzeit (optional)
 * @param categoryName der Kategoriename
 * @param categorySlug der Kategorie-Slug
 * @param locationName der Name des Veranstaltungsortes
 * @param city die Stadt
 * @param country der Ländercode
 * @param latitude die geografische Breite
 * @param longitude die geografische Länge
 * @param favorited ob das Event vom Benutzer favorisiert ist
 */
public record EventDetailResponse(
    String id,
    String name,
    String description,
    String imageUrl,
    String ticketUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String categoryName,
    String categorySlug,
    String locationName,
    String city,
    String country,
    Double latitude,
    Double longitude,
    boolean favorited) {}
