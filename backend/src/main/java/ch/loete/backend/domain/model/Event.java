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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

  @Id
  @Column(length = 8)
  private String id;

  @Column(name = "external_id", unique = true)
  private String externalId;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "ticket_url")
  private String ticketUrl;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

  @Builder.Default
  @Column(name = "source")
  private String source = "TICKETMASTER";

  // The `embedding` vector(1536) column is intentionally unmapped — JPA/Hibernate
  // has no native pgvector type support. It is managed via native SQL in EventRepository.
  @Column(name = "embedding_input", columnDefinition = "TEXT")
  private String embeddingInput;

  @Column(name = "embedded_at")
  private Instant embeddedAt;

  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    if (this.id == null) {
      this.id = NanoIdGenerator.generate();
    }
  }
}
