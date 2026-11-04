package ch.loete.backend.domain.model;

import ch.loete.backend.util.NanoIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * JPA-Entität für Benutzer-Favoriten.
 *
 * <p>Repräsentiert die Zuordnung eines Events zu einem Benutzer als Favorit. Die Kombination aus
 * Benutzer und Event ist eindeutig (Unique Constraint).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
public class Favorite {

  /** Eindeutige 8-Zeichen-NanoID des Favoriten-Eintrags. */
  @Id
  @Column(length = 8)
  private String id;

  /** Benutzer, dem der Favorit gehört. */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** Das als Favorit markierte Event. */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  /** Zeitpunkt, zu dem der Favorit erstellt wurde. */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  /** Generiert vor dem Persistieren eine NanoID, falls noch keine ID gesetzt ist. */
  @PrePersist
  public void prePersist() {
    if (this.id == null) {
      this.id = NanoIdGenerator.generate();
    }
  }
}
