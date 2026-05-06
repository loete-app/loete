package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MigrateFavoritesRequest(
    @NotNull @Size(max = 500, message = "Maximum 500 event IDs per request")
        List<String> eventIds) {}
