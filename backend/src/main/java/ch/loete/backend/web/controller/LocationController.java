package ch.loete.backend.web.controller;

import ch.loete.backend.domain.service.LocationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für Location-Endpunkte.
 *
 * <p>Stellt einen öffentlichen GET-Endpunkt für das Abrufen aller Städte mit Events bereit.
 */
@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

  /** Service für Location-Operationen. */
  private final LocationService locationService;

  /**
   * Gibt alle Städte zurück, in denen Events stattfinden.
   *
   * @return die alphabetisch sortierte Städte-Liste
   */
  @GetMapping("/cities")
  public ResponseEntity<List<String>> getCities() {
    return ResponseEntity.ok(locationService.getCities());
  }
}
