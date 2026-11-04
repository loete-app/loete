package ch.loete.backend.domain.service;

import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Favorite;
import ch.loete.backend.domain.model.User;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.process.repository.FavoriteRepository;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.response.FavoriteResponse;
import ch.loete.backend.web.dto.response.MigrateFavoritesResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final EventRepository eventRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<FavoriteResponse> getFavorites(String userId) {
    return favoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
        .map(this::toFavoriteResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<String> getFavoriteEventIds(String userId) {
    return favoriteRepository.findEventIdsByUserId(userId).stream().toList();
  }

  @Transactional
  public FavoriteResponse addFavorite(String userId, String eventId) {
    return favoriteRepository
        .findByUser_IdAndEvent_Id(userId, eventId)
        .map(this::toFavoriteResponse)
        .orElseGet(() -> createFavorite(userId, eventId));
  }

  @Transactional
  public void removeFavorite(String userId, String eventId) {
    Favorite favorite =
        favoriteRepository
            .findByUser_IdAndEvent_Id(userId, eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite", eventId));
    favoriteRepository.delete(favorite);
  }

  @Transactional
  public MigrateFavoritesResponse migrateFavorites(String userId, List<String> eventIds) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

    List<String> migrated = new ArrayList<>();
    List<String> skipped = new ArrayList<>();

    for (String eventId : eventIds) {
      if (favoriteRepository.existsByUser_IdAndEvent_Id(userId, eventId)) {
        skipped.add(eventId);
        continue;
      }
      if (eventRepository.findById(eventId).isEmpty()) {
        skipped.add(eventId);
        continue;
      }
      Event event = eventRepository.findById(eventId).orElseThrow();
      Favorite favorite = Favorite.builder().user(user).event(event).build();
      favoriteRepository.save(favorite);
      migrated.add(eventId);
    }

    return new MigrateFavoritesResponse(migrated, skipped);
  }

  private FavoriteResponse createFavorite(String userId, String eventId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    Event event =
        eventRepository
            .findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

    Favorite favorite = Favorite.builder().user(user).event(event).build();
    favorite = favoriteRepository.save(favorite);

    return toFavoriteResponse(favorite);
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
