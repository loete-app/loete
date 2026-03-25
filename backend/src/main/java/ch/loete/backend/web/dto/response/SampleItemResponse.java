package ch.loete.backend.web.dto.response;

import ch.loete.backend.domain.enums.ItemCategory;
import ch.loete.backend.domain.enums.Priority;
import java.time.Instant;

public record SampleItemResponse(
    String id,
    String title,
    String description,
    ItemCategory category,
    Priority priority,
    double price,
    Instant createdAt) {}
