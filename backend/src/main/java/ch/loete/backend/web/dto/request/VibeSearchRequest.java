package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request-DTO für die semantische Vibe-Suche.
 *
 * @param query der Suchtext / die Vibe-Beschreibung (3-200 Zeichen)
 * @param categoryId optionale Kategorie-ID zum Filtern
 * @param city optionaler Stadtname zum Filtern
 * @param dateFrom optionaler frühester Startzeitpunkt
 * @param dateTo optionaler spätester Startzeitpunkt
 * @param limit optionale maximale Anzahl Ergebnisse (1-100)
 */
public record VibeSearchRequest(
    @NotBlank @Size(min = 3, max = 200) String query,
    Long categoryId,
    String city,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    @Min(1) @Max(100) Integer limit) {}
