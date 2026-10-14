package ch.loete.backend.domain.service;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.process.client.TicketmasterClient;
import ch.loete.backend.process.client.TicketmasterClient.TicketmasterEvent;
import ch.loete.backend.process.repository.CategoryRepository;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.LocationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketmasterIntegrationService {

    private final TicketmasterClient ticketmasterClient;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public int syncEvents() {
        List<TicketmasterEvent> tmEvents = ticketmasterClient.fetchEvents();

        int count = 0;
        for (TicketmasterEvent tmEvent : tmEvents) {
            try {
                upsertEvent(tmEvent);
                count++;
            } catch (Exception e) {
                log.warn(
                        "Failed to sync event '{}' (externalId={}): {}",
                        tmEvent.name(),
                        tmEvent.id(),
                        e.getMessage());
            }
        }

        log.info("Synced {} events from Ticketmaster", count);
        return count;
    }

    private void upsertEvent(TicketmasterEvent tmEvent) {
        Category category = findOrCreateCategory(tmEvent.segmentName());
        Location location = findOrCreateLocation(tmEvent);

        Event event =
                eventRepository
                        .findByExternalId(tmEvent.id())
                        .orElseGet(() -> Event.builder().externalId(tmEvent.id()).build());

        event.setName(tmEvent.name());
        event.setDescription(tmEvent.info());
        event.setImageUrl(tmEvent.imageUrl());
        event.setTicketUrl(tmEvent.url());
        event.setStartDate(tmEvent.startDate());
        event.setCategory(category);
        event.setLocation(location);
        event.setSource("TICKETMASTER");

        eventRepository.save(event);
    }

    private Category findOrCreateCategory(String segmentName) {
        String name = (segmentName == null || segmentName.isBlank()) ? "Sonstiges" : segmentName;
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");

        return categoryRepository
                .findByNameIgnoreCase(name)
                .orElseGet(
                        () -> {
                            Category category =
                                    Category.builder().name(name).slug(slug).build();
                            return categoryRepository.save(category);
                        });
    }

    private Location findOrCreateLocation(TicketmasterEvent tmEvent) {
        String venueName = tmEvent.venueName();
        String city = tmEvent.city();

        if (venueName == null || venueName.isBlank()) {
            venueName = "Unknown Venue";
        }
        if (city == null || city.isBlank()) {
            city = "Unknown";
        }

        String finalVenueName = venueName;
        String finalCity = city;

        return locationRepository
                .findByNameAndCity(venueName, city)
                .orElseGet(
                        () -> {
                            Location location =
                                    Location.builder()
                                            .name(finalVenueName)
                                            .city(finalCity)
                                            .country(
                                                    tmEvent.countryCode() != null
                                                            ? tmEvent.countryCode()
                                                            : "CH")
                                            .latitude(tmEvent.latitude())
                                            .longitude(tmEvent.longitude())
                                            .build();
                            return locationRepository.save(location);
                        });
    }
}
