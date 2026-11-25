package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request-DTO für die Erstellung eines eigenen Events durch einen Benutzer.
 *
 * @param name der Event-Name (1-255 Zeichen)
 * @param description die optionale Beschreibung (max. 5000 Zeichen)
 * @param imageUrl die optionale Bild-URL (http/https)
 * @param ticketUrl die optionale Ticket- bzw. Info-URL (http/https)
 * @param startDate das Startdatum und die Startzeit (muss in der Zukunft liegen)
 * @param endDate das optionale Enddatum (muss nach dem Startdatum liegen)
 * @param categoryId die ID der Kategorie
 * @param locationName der Name des Veranstaltungsortes
 * @param city die Stadt des Veranstaltungsortes
 */
public record CreateEventRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 5000) String description,
    @Size(max = 1024) @Pattern(regexp = URL_PATTERN, message = URL_MESSAGE) String imageUrl,
    @Size(max = 1024) @Pattern(regexp = URL_PATTERN, message = URL_MESSAGE) String ticketUrl,
    @NotNull @Future LocalDateTime startDate,
    LocalDateTime endDate,
    @NotNull Long categoryId,
    @NotBlank @Size(max = 255) String locationName,
    @NotBlank @Size(max = 255) String city) {

  /** Erlaubt leere Werte oder absolute http(s)-URLs ohne Leerzeichen. */
  public static final String URL_PATTERN = "^$|^https?://\\S+$";

  /** Fehlermeldung bei ungültiger URL. */
  public static final String URL_MESSAGE = "must be a valid http(s) URL";

  /**
   * Prüft, ob das Enddatum (falls gesetzt) nach dem Startdatum liegt.
   *
   * @return {@code true}, wenn kein Enddatum gesetzt ist oder es nach dem Startdatum liegt
   */
  @AssertTrue(message = "must be after startDate")
  public boolean isEndDateAfterStartDate() {
    return endDate == null || startDate == null || endDate.isAfter(startDate);
  }
}
