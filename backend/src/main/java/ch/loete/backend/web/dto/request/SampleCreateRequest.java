package ch.loete.backend.web.dto.request;

import ch.loete.backend.domain.enums.ItemCategory;
import ch.loete.backend.domain.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SampleCreateRequest(
    @NotBlank String title,
    String description,
    @NotNull ItemCategory category,
    @NotNull Priority priority,
    @Positive double price) {}
