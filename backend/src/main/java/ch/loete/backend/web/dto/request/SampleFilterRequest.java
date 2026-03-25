package ch.loete.backend.web.dto.request;

import ch.loete.backend.domain.enums.ItemCategory;
import ch.loete.backend.domain.enums.Priority;

public record SampleFilterRequest(ItemCategory category, Priority priority) {}
