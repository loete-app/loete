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
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly injector = inject(Injector);
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  private readonly TOKEN_KEY = "loete_token";
  private readonly REFRESH_KEY = "loete_refresh_token";
  private readonly USER_KEY = "loete_user";

  readonly currentUser = signal<User | null>(this.loadUser());

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((res) => {
        this.handleAuthResponse(res);
        this.afterLogin();
      }),
    );
  }

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

  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return sessionStorage.getItem(this.REFRESH_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error("No refresh token"));
    }
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  private afterLogin(): void {
    const favoriteService = this.getFavoriteService();
    if (!favoriteService) return;
    favoriteService.migrateLocalFavoritesToServer().subscribe({
      complete: () => favoriteService.init(),
      error: () => favoriteService.init(),
    });
  }

  private getFavoriteService(): FavoriteService | null {
    try {
      return this.injector.get(FavoriteService);
    } catch {
      return null;
    }
  }

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

  private clearStorage(): void {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem(this.TOKEN_KEY);
      sessionStorage.removeItem(this.REFRESH_KEY);
      sessionStorage.removeItem(this.USER_KEY);
    }
  }
}
