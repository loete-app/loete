package ch.loete.backend.domain.model;

import ch.loete.backend.util.NanoIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA-Entität für registrierte Benutzer.
 *
 * <p>Repräsentiert einen Benutzer mit E-Mail, Benutzername und gehashtem Passwort. Die ID wird als
 * 8-Zeichen-NanoID generiert.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

  /** Eindeutige 8-Zeichen-NanoID des Benutzers. */
  @Id
  @Column(length = 8)
  private String id;

  /** E-Mail-Adresse des Benutzers (eindeutig). */
  @Column(unique = true, nullable = false)
  private String email;

  /** Benutzername (eindeutig). */
  @Column(unique = true, nullable = false)
  private String username;

  /** BCrypt-gehashtes Passwort des Benutzers. */
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  /** Zeitpunkt der Registrierung. */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  /** Zeitpunkt der letzten Aktualisierung. */
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  /** Generiert vor dem Persistieren eine NanoID, falls noch keine ID gesetzt ist. */
  @PrePersist
  public void prePersist() {
    if (this.id == null) {
      this.id = NanoIdGenerator.generate();
    }
  }
}
