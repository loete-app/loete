package ch.loete.backend.web.dto.response;

import java.time.Instant;

public record ErrorResponse(int status, String message, Instant timestamp) {}
