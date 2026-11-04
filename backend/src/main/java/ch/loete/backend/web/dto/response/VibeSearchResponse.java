package ch.loete.backend.web.dto.response;

import java.util.List;

public record VibeSearchResponse(List<EventResponse> results, boolean fallback) {}
