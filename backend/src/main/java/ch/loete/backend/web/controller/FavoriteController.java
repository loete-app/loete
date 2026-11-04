package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.FavoriteService;
import ch.loete.backend.process.repository.UserRepository;
import ch.loete.backend.web.dto.request.MigrateFavoritesRequest;
import ch.loete.backend.web.dto.response.FavoriteResponse;
import ch.loete.backend.web.dto.response.MigrateFavoritesResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST-Controller für Favoriten-Endpunkte.
 *
 * <p>Stellt authentifizierte Endpunkte für das Abrufen, Hinzufuegen, Entfernen und Migrieren von
 * Favoriten bereit. Alle Endpunkte erfordern eine gültige JWT-Authentifizierung.
 */
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

  /** Service für Favoriten-Operationen. */
  private final FavoriteService favoriteService;

  /** Repository für die Aufloesung des Benutzers aus dem Authentication-Objekt. */
  private final UserRepository userRepository;

  /**
   * Gibt alle Favoriten des authentifizierten Benutzers zurück.
   *
   * @param auth das Authentication-Objekt
   * @return die Favoriten-Liste
   */
  @GetMapping
  public ResponseEntity<List<FavoriteResponse>> getFavorites(Authentication auth) {
    return ResponseEntity.ok(favoriteService.getFavorites(getCurrentUserId(auth)));
  }

  /**
   * Gibt die Event-IDs aller Favoriten des authentifizierten Benutzers zurück.
   *
   * @param auth das Authentication-Objekt
   * @return die Liste der favorisierten Event-IDs
   */
  @GetMapping("/ids")
  public ResponseEntity<List<String>> getFavoriteIds(Authentication auth) {
    return ResponseEntity.ok(favoriteService.getFavoriteEventIds(getCurrentUserId(auth)));
  }

  /**
   * Fügt ein Event als Favorit hinzu.
   *
   * @param eventId die Event-ID
   * @param auth das Authentication-Objekt
   * @return der erstellte Favorit
   */
  @PostMapping("/{eventId}")
  public ResponseEntity<FavoriteResponse> addFavorite(
      @PathVariable String eventId, Authentication auth) {
    FavoriteResponse response = favoriteService.addFavorite(getCurrentUserId(auth), eventId);
    return ResponseEntity.ok(response);
  }

  /**
   * Entfernt ein Event aus den Favoriten.
   *
   * @param eventId die Event-ID
   * @param auth das Authentication-Objekt
   * @return HTTP 204 No Content
   */
  @DeleteMapping("/{eventId}")
  public ResponseEntity<Void> removeFavorite(@PathVariable String eventId, Authentication auth) {
    favoriteService.removeFavorite(getCurrentUserId(auth), eventId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Migriert lokal gespeicherte Favoriten auf den Server.
   *
   * @param request die Liste der zu migrierenden Event-IDs
   * @param auth das Authentication-Objekt
   * @return Antwort mit migrierten und übersprungenen Event-IDs
   */
  @PostMapping("/migrate")
  public ResponseEntity<MigrateFavoritesResponse> migrateFavorites(
      @Valid @RequestBody MigrateFavoritesRequest request, Authentication auth) {
    MigrateFavoritesResponse response =
        favoriteService.migrateFavorites(getCurrentUserId(auth), request.eventIds());
    return ResponseEntity.ok(response);
  }

  /**
   * Ermittelt die Benutzer-ID des authentifizierten Benutzers.
   *
   * @param auth das Authentication-Objekt
   * @return die Benutzer-ID
   * @throws ResponseStatusException wenn der Benutzer nicht gefunden wird
   */
  private String getCurrentUserId(Authentication auth) {
    String email = auth.getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
        .getId();
  }
}
