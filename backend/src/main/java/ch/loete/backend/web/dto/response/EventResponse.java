package ch.loete.backend.web.dto.response;

import java.time.LocalDateTime;

public record EventResponse(
    String id,
    String name,
    String imageUrl,
    LocalDateTime startDate,
    String categoryName,
    String locationName,
    String city) {}
