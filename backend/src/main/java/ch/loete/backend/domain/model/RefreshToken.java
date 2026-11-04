package ch.loete.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * JPA-Entität für Refresh-Tokens zur Erneuerung von JWT-Access-Tokens.
 *
 * <p>Speichert einen einmalig verwendbaren UUID-Refresh-Token mit Ablaufdatum, der einem Benutzer
 * zugeordnet ist. Wird nach Verwendung gelöscht und durch ein neues Token-Paar ersetzt
 * (Token-Rotation).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  /** Eindeutige, automatisch generierte ID des Refresh-Token-Eintrags. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Der eigentliche Refresh-Token-Wert (UUID). */
  @Column(unique = true, nullable = false)
  private String token;

  /** Benutzer, dem dieses Refresh-Token gehört. */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** Ablaufzeitpunkt des Refresh-Tokens. */
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  /** Erstellungszeitpunkt des Refresh-Tokens. */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}
