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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

  @Id
  @Column(length = 8)
  private String id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
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
