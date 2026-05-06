package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {}
