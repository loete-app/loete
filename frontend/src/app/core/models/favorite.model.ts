/**
 * Datenmodell für Benutzer-Favoriten.
 */

/** Repräsentiert einen Benutzer-Favoriten mit Event-Daten. */
export interface Favorite {
  /** Eindeutige Favoriten-ID. */
  id: string;
  /** ID des favorisierten Events. */
  eventId: string;
  /** Name des Events. */
  name: string;
  /** URL des Vorschaubildes. */
  imageUrl: string | null;
  /** Startdatum des Events als ISO-String. */
  startDate: string;
  /** Name der Kategorie. */
  categoryName: string | null;
  /** Name des Veranstaltungsortes. */
  locationName: string | null;
  /** Stadt des Veranstaltungsortes. */
  city: string | null;
  /** Erstellungszeitpunkt des Favoriten als ISO-String. */
  createdAt: string;
}
