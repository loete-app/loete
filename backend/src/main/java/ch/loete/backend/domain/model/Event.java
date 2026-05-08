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
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA-Entität für Veranstaltungen (Events).
 *
 * <p>Repräsentiert ein Event mit Titel, Beschreibung, Datum, Kategorie, Veranstaltungsort und
 * optionalem Embedding für die semantische Suche. Events werden primaer aus der Ticketmaster-API
 * importiert.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

  /** Eindeutige 8-Zeichen-NanoID des Events. */
  @Id
  @Column(length = 8)
  private String id;

  /** Externe ID aus der Ticketmaster-API für die Deduplizierung beim Import. */
  @Column(name = "external_id", unique = true)
  private String externalId;

  /** Name bzw. Titel des Events. */
  @Column(nullable = false)
  private String name;

  /** Ausführliche Beschreibung des Events. */
  @Column(columnDefinition = "TEXT")
  private String description;

  /** URL zum Vorschaubild des Events. */
  @Column(name = "image_url")
  private String imageUrl;

  /** URL zur Ticketverkaufsseite. */
  @Column(name = "ticket_url")
  private String ticketUrl;

  /** Startdatum und -uhrzeit des Events. */
  @Column(name = "start_date")
  private LocalDateTime startDate;

  /** Enddatum und -uhrzeit des Events (optional). */
  @Column(name = "end_date")
  private LocalDateTime endDate;

  /** Zugehörige Kategorie des Events (z.B. Konzert, Sport). */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  /** Veranstaltungsort des Events. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

  /** Quelle des Events (z.B. "TICKETMASTER", "TESTDATA"). */
  @Builder.Default
  @Column(name = "source")
  private String source = "TICKETMASTER";

  // The `embedding` vector(1536) column is intentionally unmapped — JPA/Hibernate
  // has no native pgvector type support. It is managed via native SQL in EventRepository.

  /** Textuelle Eingabe, die für die Embedding-Generierung verwendet wurde. */
  @Column(name = "embedding_input", columnDefinition = "TEXT")
  private String embeddingInput;

  /** Zeitpunkt der letzten Embedding-Generierung. */
  @Column(name = "embedded_at")
  private Instant embeddedAt;

  /** Zeitpunkt der Erstellung des Datensatzes. */
  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;

  /** Zeitpunkt der letzten Aktualisierung des Datensatzes. */
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
