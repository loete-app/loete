package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA-Repository für den Datenbankzugriff auf {@link User}-Entitäten. */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

  /**
   * Findet einen Benutzer anhand seiner E-Mail-Adresse.
   *
   * @param email die E-Mail-Adresse
   * @return der Benutzer, falls vorhanden
   */
  Optional<User> findByEmail(String email);

  /**
   * Findet einen Benutzer anhand seines Benutzernamens.
   *
   * @param username der Benutzername
   * @return der Benutzer, falls vorhanden
   */
  Optional<User> findByUsername(String username);

  /**
   * Prüft, ob ein Benutzer mit der angegebenen E-Mail existiert.
   *
   * @param email die E-Mail-Adresse
   * @return {@code true} wenn die E-Mail bereits vergeben ist
   */
  boolean existsByEmail(String email);

  /**
   * Prüft, ob ein Benutzer mit dem angegebenen Benutzernamen existiert.
   *
   * @param username der Benutzername
   * @return {@code true} wenn der Benutzername bereits vergeben ist
   */
  boolean existsByUsername(String username);
}
