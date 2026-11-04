/**
 * Datenmodelle für Events und Paginierung.
 */

/** Repräsentiert ein Event in der Listenansicht. */
export interface Event {
  /** Eindeutige Event-ID. */
  id: string;
  /** Name des Events. */
  name: string;
  /** URL des Vorschaubildes. */
  imageUrl: string;
  /** Startdatum und -uhrzeit als ISO-String. */
  startDate: string;
  /** Name der Kategorie. */
  categoryName: string;
  /** Name des Veranstaltungsortes. */
  locationName: string;
  /** Stadt des Veranstaltungsortes. */
  city: string;
}

/** Repräsentiert ein Event in der Detailansicht. */
export interface EventDetail {
  /** Eindeutige Event-ID. */
  id: string;
  /** Name des Events. */
  name: string;
  /** Ausführliche Beschreibung. */
  description: string | null;
  /** URL des Vorschaubildes. */
  imageUrl: string | null;
  /** URL zur Ticketverkaufsseite. */
  ticketUrl: string | null;
  /** Startdatum und -uhrzeit als ISO-String. */
  startDate: string;
  /** Enddatum und -uhrzeit als ISO-String. */
  endDate: string | null;
  /** Name der Kategorie. */
  categoryName: string | null;
  /** URL-freundlicher Kategorie-Slug. */
  categorySlug: string | null;
  /** Name des Veranstaltungsortes. */
  locationName: string | null;
  /** Stadt des Veranstaltungsortes. */
  city: string | null;
  /** Ländercode. */
  country: string | null;
  /** Geografische Breite. */
  latitude: number | null;
  /** Geografische Länge. */
  longitude: number | null;
  /** Ob das Event vom Benutzer favorisiert ist. */
  favorited: boolean;
}

/** Generische paginierte API-Antwort. */
export interface PagedResponse<T> {
  /** Elemente der aktuellen Seite. */
  content: T[];
  /** Aktuelle Seitennummer (0-basiert). */
  page: number;
  /** Seitengrösse. */
  size: number;
  /** Gesamtanzahl der Elemente. */
  totalElements: number;
  /** Gesamtanzahl der Seiten. */
  totalPages: number;
  /** Ob dies die letzte Seite ist. */
  last: boolean;
}

/** Filterkriterien für Event-Abfragen. */
export interface EventFilter {
  /** Seitennummer. */
  page?: number;
  /** Seitengrösse. */
  size?: number;
  /** Kategorie-ID zum Filtern. */
  categoryId?: number | null;
  /** Stadtname zum Filtern. */
  city?: string | null;
  /** Frühester Startzeitpunkt (ISO-String). */
  dateFrom?: string | null;
  /** Spätester Startzeitpunkt (ISO-String). */
  dateTo?: string | null;
  /** Suchbegriff für den Event-Namen. */
  search?: string | null;
}
