package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record VibeSearchRequest(
    @NotBlank @Size(min = 3, max = 200) String query,
    Long categoryId,
    String city,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    @Min(1) @Max(100) Integer limit) {}
