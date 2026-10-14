package ch.loete.backend.web.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

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
