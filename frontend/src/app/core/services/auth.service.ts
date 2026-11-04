/**
 * Service für Authentifizierung und Session-Verwaltung.
 *
 * Verwaltet Login, Registrierung, Logout, Token-Refresh und
 * die Speicherung der Session-Daten im SessionStorage.
 * Triggert nach dem Login die Migration lokaler Favoriten.
 */
import {
  Injectable,
  Injector,
  PLATFORM_ID,
  inject,
  signal,
} from "@angular/core";
import { isPlatformBrowser } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { Router } from "@angular/router";
import { Observable, tap, throwError } from "rxjs";
import { environment } from "../../../environments/environment";
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from "../models/auth.model";
import { FavoriteService } from "./favorite.service";

@Injectable({ providedIn: "root" })
export class AuthService {
  /** HTTP-Client für API-Aufrufe. */
  private readonly http = inject(HttpClient);
  /** Angular Router für Navigationen. */
  private readonly router = inject(Router);
  /** Plattform-ID zur Browser/Server-Unterscheidung. */
  private readonly platformId = inject(PLATFORM_ID);
  /** Injector für die lazy Aufloesung des FavoriteService. */
  private readonly injector = inject(Injector);
  /** Basis-URL der Auth-API-Endpunkte. */
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  /** SessionStorage-Schlüssel für das Access-Token. */
  private readonly TOKEN_KEY = "loete_token";
  /** SessionStorage-Schlüssel für das Refresh-Token. */
  private readonly REFRESH_KEY = "loete_refresh_token";
  /** SessionStorage-Schlüssel für die Benutzerdaten. */
  private readonly USER_KEY = "loete_user";

  /** Signal mit dem aktuell eingeloggten Benutzer (oder null). */
  readonly currentUser = signal<User | null>(this.loadUser());

  /**
   * Meldet einen Benutzer an.
   *
   * @param request die Login-Daten
   * @returns Observable mit der Authentifizierungsantwort
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((res) => {
        this.handleAuthResponse(res);
        this.afterLogin();
      }),
    );
  }

  /**
   * Registriert einen neuen Benutzer.
   *
   * @param request die Registrierungsdaten
   * @returns Observable mit der Authentifizierungsantwort
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, request)
      .pipe(
        tap((res) => {
          this.handleAuthResponse(res);
          this.afterLogin();
        }),
      );
  }

  /** Meldet den Benutzer ab, löscht die Session und navigiert zum Login. */
  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe();
    }
    this.clearStorage();
    this.currentUser.set(null);
    this.getFavoriteService()?.reset();
    this.router.navigate(["/login"]);
  }

  /**
   * Gibt das gespeicherte Access-Token zurück.
   *
   * @returns das Token oder null (auf dem Server immer null)
   */
  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Gibt das gespeicherte Refresh-Token zurück.
   *
   * @returns das Refresh-Token oder null
   */
  getRefreshToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return sessionStorage.getItem(this.REFRESH_KEY);
  }

  /**
   * Prüft, ob der Benutzer authentifiziert ist.
   *
   * @returns true wenn ein Access-Token vorhanden ist
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Erneuert das Access-Token über den Refresh-Endpunkt.
   *
   * @returns Observable mit der neuen Authentifizierungsantwort
   */
  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error("No refresh token"));
    }
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  /** Migriert nach dem Login lokale Favoriten auf den Server. */
  private afterLogin(): void {
    const favoriteService = this.getFavoriteService();
    if (!favoriteService) return;
    favoriteService.migrateLocalFavoritesToServer().subscribe({
      complete: () => favoriteService.init(),
      error: () => favoriteService.init(),
    });
  }

  /**
   * Lädt den FavoriteService lazy über den Injector.
   *
   * @returns die FavoriteService-Instanz oder null
   */
  private getFavoriteService(): FavoriteService | null {
    try {
      return this.injector.get(FavoriteService);
    } catch {
      return null;
    }
  }

  /**
   * Speichert die Authentifizierungsdaten im SessionStorage.
   *
   * @param res die Authentifizierungsantwort
   */
  private handleAuthResponse(res: AuthResponse): void {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.setItem(this.TOKEN_KEY, res.accessToken);
      sessionStorage.setItem(this.REFRESH_KEY, res.refreshToken);
      const user: User = {
        id: res.userId,
        email: res.email,
        username: res.username,
      };
      sessionStorage.setItem(this.USER_KEY, JSON.stringify(user));
      this.currentUser.set(user);
    }
  }

  /**
   * Lädt den Benutzer aus dem SessionStorage (beim Service-Start).
   *
   * @returns der gespeicherte Benutzer oder null
   */
  private loadUser(): User | null {
    if (typeof window === "undefined") return null;
    const stored = sessionStorage.getItem(this.USER_KEY);
    if (!stored) return null;
    try {
      return JSON.parse(stored);
    } catch {
      return null;
    }
  }

  /** Löscht alle Session-Daten aus dem SessionStorage. */
  private clearStorage(): void {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem(this.TOKEN_KEY);
      sessionStorage.removeItem(this.REFRESH_KEY);
      sessionStorage.removeItem(this.USER_KEY);
    }
  }
}
