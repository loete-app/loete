package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.FavoriteService;
import ch.loete.backend.web.dto.response.FavoriteResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;

  @GetMapping
  public ResponseEntity<List<FavoriteResponse>> getFavorites(
      @RequestHeader(value = "X-Client-Id", required = false) String clientId) {
    return ResponseEntity.ok(favoriteService.getFavorites(requireClientId(clientId)));
  }

  @GetMapping("/ids")
  public ResponseEntity<List<String>> getFavoriteIds(
      @RequestHeader(value = "X-Client-Id", required = false) String clientId) {
    return ResponseEntity.ok(favoriteService.getFavoriteEventIds(requireClientId(clientId)));
  }

  @PostMapping("/{eventId}")
  public ResponseEntity<FavoriteResponse> addFavorite(
      @PathVariable String eventId,
      @RequestHeader(value = "X-Client-Id", required = false) String clientId) {
    FavoriteResponse response = favoriteService.addFavorite(requireClientId(clientId), eventId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{eventId}")
  public ResponseEntity<Void> removeFavorite(
      @PathVariable String eventId,
      @RequestHeader(value = "X-Client-Id", required = false) String clientId) {
    favoriteService.removeFavorite(requireClientId(clientId), eventId);
    return ResponseEntity.noContent().build();
  }

  private String requireClientId(String clientId) {
    if (clientId == null || clientId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-Client-Id header");
    }
    return clientId;
  }
}
