package ch.loete.backend.web.dto.response;

import java.time.LocalDateTime;

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
