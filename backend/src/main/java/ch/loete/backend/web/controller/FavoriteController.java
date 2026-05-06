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

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<List<FavoriteResponse>> getFavorites(Authentication auth) {
    return ResponseEntity.ok(favoriteService.getFavorites(getCurrentUserId(auth)));
  }

  @GetMapping("/ids")
  public ResponseEntity<List<String>> getFavoriteIds(Authentication auth) {
    return ResponseEntity.ok(favoriteService.getFavoriteEventIds(getCurrentUserId(auth)));
  }

  @PostMapping("/{eventId}")
  public ResponseEntity<FavoriteResponse> addFavorite(
      @PathVariable String eventId, Authentication auth) {
    FavoriteResponse response = favoriteService.addFavorite(getCurrentUserId(auth), eventId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{eventId}")
  public ResponseEntity<Void> removeFavorite(@PathVariable String eventId, Authentication auth) {
    favoriteService.removeFavorite(getCurrentUserId(auth), eventId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/migrate")
  public ResponseEntity<MigrateFavoritesResponse> migrateFavorites(
      @Valid @RequestBody MigrateFavoritesRequest request, Authentication auth) {
    MigrateFavoritesResponse response =
        favoriteService.migrateFavorites(getCurrentUserId(auth), request.eventIds());
    return ResponseEntity.ok(response);
  }

  private String getCurrentUserId(Authentication auth) {
    String email = auth.getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
        .getId();
  }
}
