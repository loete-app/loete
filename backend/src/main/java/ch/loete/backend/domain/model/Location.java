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
 * JPA-Entität für Veranstaltungsorte (Locations).
 *
 * <p>Repräsentiert einen physischen Veranstaltungsort mit Name, Stadt, Land und optionalen
 * Geokoordinaten. Locations werden beim Import aus Ticketmaster automatisch erstellt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "locations")
public class Location {

  /** Eindeutige, automatisch generierte ID der Location. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Name des Veranstaltungsortes (z.B. "Hallenstadion"). */
  @Column(nullable = false)
  private String name;

  /** Stadt, in der sich der Veranstaltungsort befindet. */
  private String city;

  /** Ländercode des Veranstaltungsortes (Standard: "CH"). */
  @Builder.Default
  @Column(name = "country")
  private String country = "CH";

  /** Geografische Breite (Latitude) des Veranstaltungsortes. */
  private Double latitude;

  /** Geografische Länge (Longitude) des Veranstaltungsortes. */
  private Double longitude;
}
