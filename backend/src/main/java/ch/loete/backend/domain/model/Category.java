package ch.loete.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA-Entität für Event-Kategorien.
 *
 * <p>Repräsentiert eine Kategorie wie "Konzert", "Sport" oder "Theater", der Events zugeordnet
 * werden. Kategorien werden per Flyway-Migration vordefiniert und von der Ticketmaster-Integration
 * gemappt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

  /** Eindeutige, automatisch generierte ID der Kategorie. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Anzeigename der Kategorie (z.B. "Konzert", "Sport"). */
  @Column(unique = true, nullable = false)
  private String name;

  /** URL-freundlicher Slug der Kategorie (z.B. "konzert", "sport"). */
  @Column(unique = true, nullable = false)
  private String slug;
}
