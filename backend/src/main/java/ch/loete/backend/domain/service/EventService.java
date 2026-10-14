package ch.loete.backend.domain.service;

import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.FavoriteRepository;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.response.EventDetailResponse;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final FavoriteRepository favoriteRepository;

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getEvents(EventFilterRequest filter) {
        Specification<Event> spec =
                Specification.allOf(
                        EventSpecifications.hasCategory(filter.categoryId()),
                        EventSpecifications.inCity(filter.city()),
                        EventSpecifications.startsAfter(filter.dateFrom()),
                        EventSpecifications.startsBefore(filter.dateTo()),
                        EventSpecifications.nameContains(filter.search()));

        Page<Event> result =
                eventRepository.findAll(
                        spec,
                        PageRequest.of(
                                filter.page(), filter.size(), Sort.by("startDate").ascending()));

        return new PagedResponse<>(
                result.getContent().stream().map(this::toEventResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast());
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEvent(String id, String clientId) {
        Event event =
                eventRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Event", id));

        boolean favorited =
                clientId != null && favoriteRepository.existsByClientIdAndEventId(clientId, id);

        return toEventDetailResponse(event, favorited);
    }

    private EventResponse toEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getImageUrl(),
                event.getStartDate(),
                event.getCategory() != null ? event.getCategory().getName() : null,
                event.getLocation() != null ? event.getLocation().getName() : null,
                event.getLocation() != null ? event.getLocation().getCity() : null);
    }

    private EventDetailResponse toEventDetailResponse(Event event, boolean favorited) {
        return new EventDetailResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getImageUrl(),
                event.getTicketUrl(),
                event.getStartDate(),
                event.getEndDate(),
                event.getCategory() != null ? event.getCategory().getName() : null,
                event.getCategory() != null ? event.getCategory().getSlug() : null,
                event.getLocation() != null ? event.getLocation().getName() : null,
                event.getLocation() != null ? event.getLocation().getCity() : null,
                event.getLocation() != null ? event.getLocation().getCountry() : null,
                event.getLocation() != null ? event.getLocation().getLatitude() : null,
                event.getLocation() != null ? event.getLocation().getLongitude() : null,
                favorited);
    }
}
