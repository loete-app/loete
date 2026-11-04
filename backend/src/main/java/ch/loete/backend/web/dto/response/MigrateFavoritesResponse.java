package ch.loete.backend.web.dto.response;

import java.util.List;

public record MigrateFavoritesResponse(List<String> migrated, List<String> skipped) {}
