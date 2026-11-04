import { Injectable, PLATFORM_ID, inject, signal } from "@angular/core";
import { isPlatformBrowser } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { Observable, forkJoin, of, tap } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { environment } from "../../../environments/environment";
import { Favorite } from "../models/favorite.model";
import { Event } from "../models/event.model";
import { AuthService } from "./auth.service";

@Injectable({ providedIn: "root" })
export class FavoriteService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly apiUrl = `${environment.apiUrl}/favorites`;
  private readonly STORAGE_KEY = "loete_favorites";

  readonly favoriteIds = signal<Set<string>>(new Set());
  private loaded = false;

  init(): void {
    if (this.authService.isAuthenticated()) {
      this.loadFromServer();
    } else {
      this.loadFromLocalStorage();
    }
  }

  reset(): void {
    this.loaded = false;
    this.favoriteIds.set(new Set());
  }

  isFavorite(eventId: string): boolean {
    return this.favoriteIds().has(eventId);
  }

  getFavorites(): Observable<Favorite[]> {
    if (this.authService.isAuthenticated()) {
      return this.http.get<Favorite[]>(this.apiUrl);
    }

    const ids = [...this.favoriteIds()];
    if (ids.length === 0) {
      return of([]);
    }

    return forkJoin(
      ids.map((id) =>
        this.http
          .get<Event>(`${environment.apiUrl}/events/${id}`)
          .pipe(catchError(() => of(null))),
      ),
    ).pipe(
      map((results) => {
        const validIds = new Set<string>();
        const favorites: Favorite[] = [];

        for (let i = 0; i < results.length; i++) {
          const event = results[i];
          if (event) {
            validIds.add(ids[i]);
            favorites.push({
              id: ids[i],
              eventId: ids[i],
              name: event.name,
              imageUrl: event.imageUrl,
              startDate: event.startDate,
              categoryName: event.categoryName,
              locationName: event.locationName,
              city: event.city,
              createdAt: "",
            });
          }
        }

        if (validIds.size !== ids.length) {
          this.favoriteIds.set(validIds);
          this.saveToLocalStorage(validIds);
        }

        return favorites;
      }),
    );
  }

  addFavorite(eventId: string): Observable<Favorite> {
    if (this.authService.isAuthenticated()) {
      return this.http
        .post<Favorite>(`${this.apiUrl}/${eventId}`, null)
        .pipe(tap(() => this.updateSignal((set) => set.add(eventId))));
    }

    this.updateSignal((set) => set.add(eventId));
    this.saveToLocalStorage(this.favoriteIds());
    return of({
      id: eventId,
      eventId,
      name: "",
      imageUrl: null,
      startDate: "",
      categoryName: null,
      locationName: null,
      city: null,
      createdAt: "",
    });
  }

  removeFavorite(eventId: string): Observable<void> {
    if (this.authService.isAuthenticated()) {
      return this.http
        .delete<void>(`${this.apiUrl}/${eventId}`)
        .pipe(tap(() => this.updateSignal((set) => set.delete(eventId))));
    }

    this.updateSignal((set) => set.delete(eventId));
    this.saveToLocalStorage(this.favoriteIds());
    return of(undefined);
  }

  migrateLocalFavoritesToServer(): Observable<unknown> {
    if (!isPlatformBrowser(this.platformId)) return of(null);

    const stored = localStorage.getItem(this.STORAGE_KEY);
    if (!stored) return of(null);

    let eventIds: string[];
    try {
      eventIds = JSON.parse(stored);
    } catch {
      return of(null);
    }

    if (!Array.isArray(eventIds) || eventIds.length === 0) return of(null);

    return this.http
      .post<{
        migrated: string[];
        skipped: string[];
      }>(`${this.apiUrl}/migrate`, { eventIds })
      .pipe(
        tap(() => {
          localStorage.removeItem(this.STORAGE_KEY);
          this.loadFromServer();
        }),
        catchError((err) => {
          console.warn("Failed to migrate local favorites to server:", err);
          return of(null);
        }),
      );
  }

  private loadFromServer(): void {
    if (this.loaded) return;
    this.loaded = true;
    this.http.get<string[]>(`${this.apiUrl}/ids`).subscribe({
      next: (ids) => this.favoriteIds.set(new Set(ids)),
      error: () => this.favoriteIds.set(new Set()),
    });
  }

  private loadFromLocalStorage(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.loaded = true;
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (stored) {
        const ids: string[] = JSON.parse(stored);
        this.favoriteIds.set(new Set(ids));
      }
    } catch {
      this.favoriteIds.set(new Set());
    }
  }

  private saveToLocalStorage(ids: Set<string>): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify([...ids]));
  }

  private updateSignal(fn: (set: Set<string>) => void): void {
    this.favoriteIds.update((set) => {
      const next = new Set(set);
      fn(next);
      return next;
    });
  }
}
