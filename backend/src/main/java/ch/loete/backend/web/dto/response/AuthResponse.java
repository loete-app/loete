package ch.loete.backend.web.dto.response;

public record AuthResponse(
    String accessToken, String refreshToken, String userId, String username, String email) {}
