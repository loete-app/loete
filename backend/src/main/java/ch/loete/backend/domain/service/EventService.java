package ch.loete.backend.domain.service;

import ch.loete.backend.domain.model.Event;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getEvents(int page, int size) {
        Page<Event> result =
                eventRepository.findAll(
                        PageRequest.of(page, size, Sort.by("startDate").ascending()));

        return new PagedResponse<>(
                result.getContent().stream().map(this::toEventResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast());
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
}
