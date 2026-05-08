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

/**
 * Service für die Verwaltung von Benutzer-Favoriten.
 *
 * <p>Ermöglicht das Hinzufuegen, Entfernen und Abfragen von Favoriten sowie die Migration von lokal
 * gespeicherten Favoriten (anonyme Nutzung) auf den Server nach der Registrierung/Anmeldung.
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

  /** Repository für den Zugriff auf Favoriten-Daten. */
  private final FavoriteRepository favoriteRepository;

  /** Repository für den Zugriff auf Event-Daten. */
  private final EventRepository eventRepository;

  /** Repository für den Zugriff auf Benutzer-Daten. */
  private final UserRepository userRepository;

  /**
   * Gibt alle Favoriten eines Benutzers zurück, sortiert nach Erstellungsdatum (neueste zuerst).
   *
   * @param userId die Benutzer-ID
   * @return Liste der Favoriten als Response-DTOs
   */
  @Transactional(readOnly = true)
  public List<FavoriteResponse> getFavorites(String userId) {
    return favoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
        .map(this::toFavoriteResponse)
        .toList();
  }

  /**
   * Gibt die Event-IDs aller Favoriten eines Benutzers zurück.
   *
   * @param userId die Benutzer-ID
   * @return Liste der favorisierten Event-IDs
   */
  @Transactional(readOnly = true)
  public List<String> getFavoriteEventIds(String userId) {
    return favoriteRepository.findEventIdsByUserId(userId).stream().toList();
  }

  /**
   * Fügt ein Event als Favorit hinzu. Falls bereits favorisiert, wird der bestehende Favorit
   * zurückgegeben (idempotent).
   *
   * @param userId die Benutzer-ID
   * @param eventId die Event-ID
   * @return der erstellte oder bestehende Favorit
   */
  @Transactional
  public FavoriteResponse addFavorite(String userId, String eventId) {
    return favoriteRepository
        .findByUser_IdAndEvent_Id(userId, eventId)
        .map(this::toFavoriteResponse)
        .orElseGet(() -> createFavorite(userId, eventId));
  }

  /**
   * Entfernt ein Event aus den Favoriten eines Benutzers.
   *
   * @param userId die Benutzer-ID
   * @param eventId die Event-ID
   * @throws ResourceNotFoundException wenn der Favorit nicht existiert
   */
  @Transactional
  public void removeFavorite(String userId, String eventId) {
    Favorite favorite =
        favoriteRepository
            .findByUser_IdAndEvent_Id(userId, eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite", eventId));
    favoriteRepository.delete(favorite);
  }

  /**
   * Migriert eine Liste von lokal gespeicherten Favoriten auf den Server.
   *
   * <p>Bereits existierende Favoriten und nicht gefundene Events werden übersprungen.
   *
   * @param userId die Benutzer-ID
   * @param eventIds die zu migrierenden Event-IDs
   * @return Antwort mit migrierten und übersprungenen Event-IDs
   */
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

  /**
   * Erstellt einen neuen Favoriten-Eintrag in der Datenbank.
   *
   * @param userId die Benutzer-ID
   * @param eventId die Event-ID
   * @return der erstellte Favorit als Response-DTO
   */
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

  /**
   * Konvertiert eine Favorite-Entität in ein FavoriteResponse-DTO.
   *
   * @param favorite die Favorit-Entität
   * @return das Response-DTO
   */
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
