/**
 * Datenmodell für Event-Kategorien.
 */

/** Repräsentiert eine Event-Kategorie. */
export interface Category {
  /** Eindeutige Kategorie-ID. */
  id: number;
  /** Anzeigename der Kategorie. */
  name: string;
  /** URL-freundlicher Slug. */
  slug: string;
}
