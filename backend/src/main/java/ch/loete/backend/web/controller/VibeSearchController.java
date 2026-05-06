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

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class VibeSearchController {

  private final VibeSearchService vibeSearchService;

  @PostMapping("/vibe")
  public ResponseEntity<VibeSearchResponse> vibeSearch(
      @Valid @RequestBody VibeSearchRequest request) {
    return ResponseEntity.ok(vibeSearchService.search(request));
  }
}
