package ch.loete.backend.domain.service;

import ch.loete.backend.domain.model.Event;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

final class EventSpecifications {

  private EventSpecifications() {}

  static Specification<Event> hasCategory(Long categoryId) {
    if (categoryId == null) {
      return null;
    }
    return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
  }

  static Specification<Event> inCity(String city) {
    if (city == null || city.isBlank()) {
      return null;
    }
    return (root, query, cb) ->
        cb.equal(cb.lower(root.get("location").get("city")), city.toLowerCase());
  }

  static Specification<Event> startsAfter(LocalDateTime dateFrom) {
    if (dateFrom == null) {
      return null;
    }
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), dateFrom);
  }

  static Specification<Event> startsBefore(LocalDateTime dateTo) {
    if (dateTo == null) {
      return null;
    }
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), dateTo);
  }

  static Specification<Event> nameContains(String search) {
    if (search == null || search.isBlank()) {
      return null;
    }
    String pattern = "%" + search.toLowerCase() + "%";
    return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
  }
}
