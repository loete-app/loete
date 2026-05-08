package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request-DTO für die Migration von lokal gespeicherten Favoriten auf den Server.
 *
 * @param eventIds die zu migrierenden Event-IDs (maximal 500)
 */
public record MigrateFavoritesRequest(
    @NotNull @Size(max = 500, message = "Maximum 500 event IDs per request")
        List<String> eventIds) {}
