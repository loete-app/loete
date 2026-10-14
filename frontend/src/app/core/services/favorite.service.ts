import { Injectable, inject, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, tap } from "rxjs";
import { environment } from "../../../environments/environment";
import { Favorite } from "../models/favorite.model";

@Injectable({ providedIn: "root" })
export class FavoriteService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/favorites`;

  readonly favoriteIds = signal<Set<string>>(new Set());
  private loaded = false;

  loadIds(): void {
    if (this.loaded) return;
    this.loaded = true;
    this.http.get<string[]>(`${this.apiUrl}/ids`).subscribe({
      next: (ids) => this.favoriteIds.set(new Set(ids)),
      error: () => this.favoriteIds.set(new Set()),
    });
  }

  isFavorite(eventId: string): boolean {
    return this.favoriteIds().has(eventId);
  }

  getFavorites(): Observable<Favorite[]> {
    return this.http.get<Favorite[]>(this.apiUrl);
  }

  addFavorite(eventId: string): Observable<Favorite> {
    return this.http.post<Favorite>(`${this.apiUrl}/${eventId}`, null).pipe(
      tap(() =>
        this.favoriteIds.update((set) => {
          const next = new Set(set);
          next.add(eventId);
          return next;
        }),
      ),
    );
  }

  removeFavorite(eventId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${eventId}`).pipe(
      tap(() =>
        this.favoriteIds.update((set) => {
          const next = new Set(set);
          next.delete(eventId);
          return next;
        }),
      ),
    );
  }
}
