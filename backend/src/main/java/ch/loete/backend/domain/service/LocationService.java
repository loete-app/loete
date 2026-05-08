package ch.loete.backend.domain.service;

import ch.loete.backend.process.repository.LocationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service für Location-bezogene Operationen.
 *
 * <p>Stellt Methoden zum Abrufen der verfügbaren Städte bereit, in denen Events stattfinden.
 */
@Service
@RequiredArgsConstructor
public class LocationService {

  /** Repository für den Zugriff auf Location-Daten. */
  private final LocationRepository locationRepository;

  /**
   * Gibt alle Städte zurück, in denen mindestens ein Event stattfindet.
   *
   * @return alphabetisch sortierte Liste der Staedtenamen
   */
  @Transactional(readOnly = true)
  public List<String> getCities() {
    return locationRepository.findDistinctCitiesWithEvents();
  }
}
