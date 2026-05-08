package ch.loete.backend.web.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request-DTO für die Token-Erneuerung und den Logout.
 *
 * @param refreshToken das aktuelle Refresh-Token
 */
public record RefreshRequest(@NotBlank String refreshToken) {}
