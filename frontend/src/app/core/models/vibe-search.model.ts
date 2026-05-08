/**
 * Datenmodelle für die Vibe-Suche.
 */
import { Event } from "./event.model";

/** Request-Daten für die semantische Vibe-Suche. */
export interface VibeSearchRequest {
  /** Suchtext / Vibe-Beschreibung. */
  query: string;
  /** Optionale Kategorie-ID zum Filtern. */
  categoryId?: number | null;
  /** Optionaler Stadtname zum Filtern. */
  city?: string | null;
  /** Optionaler frühester Startzeitpunkt (ISO-String). */
  dateFrom?: string | null;
  /** Optionaler spätester Startzeitpunkt (ISO-String). */
  dateTo?: string | null;
  /** Optionale maximale Anzahl Ergebnisse. */
  limit?: number | null;
}

/** Antwort der Vibe-Suche. */
export interface VibeSearchResponse {
  /** Liste der gefundenen Events. */
  results: Event[];
  /** Ob nur Keyword-Ergebnisse verwendet wurden (Fallback). */
  fallback: boolean;
}
