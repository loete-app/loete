package ch.loete.backend.domain.service;

import ch.loete.backend.domain.exception.DuplicateResourceException;
import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Favorite;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.FavoriteRepository;
import ch.loete.backend.web.dto.response.FavoriteResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final EventRepository eventRepository;

  @Transactional(readOnly = true)
  public List<FavoriteResponse> getFavorites(String clientId) {
    return favoriteRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
        .map(this::toFavoriteResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<String> getFavoriteEventIds(String clientId) {
    return favoriteRepository.findEventIdsByClientId(clientId).stream().toList();
  }

  @Transactional
  public FavoriteResponse addFavorite(String clientId, String eventId) {
    if (favoriteRepository.existsByClientIdAndEventId(clientId, eventId)) {
      throw new DuplicateResourceException("Favorite", "eventId", eventId);
    }

    Event event =
        eventRepository
            .findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

    Favorite favorite = Favorite.builder().clientId(clientId).event(event).build();
    favorite = favoriteRepository.save(favorite);

    return toFavoriteResponse(favorite);
  }

  @Transactional
  public void removeFavorite(String clientId, String eventId) {
    Favorite favorite =
        favoriteRepository
            .findByClientIdAndEventId(clientId, eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite", eventId));
    favoriteRepository.delete(favorite);
  }

  private FavoriteResponse toFavoriteResponse(Favorite favorite) {
    Event event = favorite.getEvent();
    return new FavoriteResponse(
        favorite.getId(),
        event.getId(),
        event.getName(),
        event.getImageUrl(),
        event.getStartDate(),
        event.getCategory() != null ? event.getCategory().getName() : null,
        event.getLocation() != null ? event.getLocation().getName() : null,
        event.getLocation() != null ? event.getLocation().getCity() : null,
        favorite.getCreatedAt());
  }
}
