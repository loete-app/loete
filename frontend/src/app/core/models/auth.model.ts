/**
 * Datenmodelle für die Authentifizierung.
 */

/** Request-Daten für die Benutzer-Anmeldung. */
export interface LoginRequest {
  /** E-Mail-Adresse des Benutzers. */
  email: string;
  /** Passwort des Benutzers. */
  password: string;
}

/** Request-Daten für die Benutzer-Registrierung. */
export interface RegisterRequest {
  /** E-Mail-Adresse des Benutzers. */
  email: string;
  /** Gewünschter Benutzername. */
  username: string;
  /** Gewünschtes Passwort. */
  password: string;
}

/** Antwort des Servers nach erfolgreicher Authentifizierung. */
export interface AuthResponse {
  /** JWT-Access-Token. */
  accessToken: string;
  /** UUID-Refresh-Token. */
  refreshToken: string;
  /** Eindeutige Benutzer-ID. */
  userId: string;
  /** Benutzername. */
  username: string;
  /** E-Mail-Adresse. */
  email: string;
}

/** Repräsentiert einen eingeloggten Benutzer. */
export interface User {
  /** Eindeutige Benutzer-ID. */
  id: string;
  /** E-Mail-Adresse. */
  email: string;
  /** Benutzername. */
  username: string;
}
