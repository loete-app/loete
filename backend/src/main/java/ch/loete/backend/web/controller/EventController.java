package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.EventService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.response.EventDetailResponse;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

  private final EventService eventService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<PagedResponse<EventResponse>> getEvents(
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String city,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateTo,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    EventFilterRequest filter =
        new EventFilterRequest(categoryId, city, dateFrom, dateTo, search, page, size);
    return ResponseEntity.ok(eventService.getEvents(filter));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EventDetailResponse> getEvent(
      @PathVariable String id, Authentication auth) {
    String userId = null;
    if (auth != null) {
      userId = userRepository.findByEmail(auth.getName()).map(user -> user.getId()).orElse(null);
    }
    return ResponseEntity.ok(eventService.getEvent(id, userId));
  }
}
