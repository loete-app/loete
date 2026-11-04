package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.VibeSearchService;
import ch.loete.backend.web.dto.request.VibeSearchRequest;
import ch.loete.backend.web.dto.response.VibeSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für den Vibe-Search-Endpunkt.
 *
 * <p>Stellt einen öffentlichen POST-Endpunkt für die semantische Vibe-Suche nach Events bereit.
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class VibeSearchController {

  /** Service für die Vibe-Suche. */
  private final VibeSearchService vibeSearchService;

  /**
   * Führt eine semantische Vibe-Suche nach Events durch.
   *
   * @param request die Suchanfrage mit Query-Text und optionalen Filtern
   * @return die Suchergebnisse
   */
  @PostMapping("/vibe")
  public ResponseEntity<VibeSearchResponse> vibeSearch(
      @Valid @RequestBody VibeSearchRequest request) {
    return ResponseEntity.ok(vibeSearchService.search(request));
  }
}
