package ch.loete.backend.web.dto.request;

import java.time.LocalDateTime;

public record EventFilterRequest(
        Long categoryId,
        String city,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        String search,
        Integer page,
        Integer size) {

    public EventFilterRequest {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
        if (city != null && city.isBlank()) {
            city = null;
        }
        if (search != null && search.isBlank()) {
            search = null;
        }
    }
}
