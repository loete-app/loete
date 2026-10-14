import { Component, inject, OnInit, signal } from "@angular/core";
import { DatePipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import { FavoriteService } from "@/core/services/favorite.service";
import { Favorite } from "@/core/models/favorite.model";
import {
  LucideAngularModule,
  Heart,
  Trash2,
  Calendar,
  MapPin,
  AlertTriangle,
} from "lucide-angular";

@Component({
  selector: "app-favorites",
  imports: [DatePipe, RouterLink, LucideAngularModule],
  template: `
    <div class="page">
      <header class="head">
        <h1>Meine Favoriten</h1>
        <p class="subtitle">Deine gespeicherten Events.</p>
      </header>

      @if (loading()) {
        <p class="state">Favoriten werden geladen...</p>
      } @else if (error()) {
        <div class="error">
          <i-lucide [img]="AlertIcon" [size]="40" />
          <h3>Favoriten konnten nicht geladen werden</h3>
          <p>{{ error() }}</p>
          <button (click)="reload()">Erneut versuchen</button>
        </div>
      } @else if (favorites().length === 0) {
        <div class="empty">
          <i-lucide [img]="HeartIcon" [size]="48" class="empty-icon" />
          <h3>Noch keine Favoriten</h3>
          <p>
            Markiere Events mit dem Herz-Symbol, um sie hier wiederzufinden.
          </p>
          <a routerLink="/" class="cta">Events entdecken</a>
        </div>
      } @else {
        <ul class="list">
          @for (fav of favorites(); track fav.id) {
            <li class="row">
              <a [routerLink]="['/events', fav.eventId]" class="row-link">
                <img
                  [src]="fav.imageUrl || fallbackImage"
                  [alt]="fav.name"
                  (error)="onImageError($event)"
                  loading="lazy"
                />
                <div class="info">
                  <h3>{{ fav.name }}</h3>
                  <div class="meta">
                    <span>
                      <i-lucide [img]="CalendarIcon" [size]="13" />
                      {{ fav.startDate | date: "d. MMM y, HH:mm" }} Uhr
                    </span>
                    @if (fav.city) {
                      <span>
                        <i-lucide [img]="MapPinIcon" [size]="13" />
                        {{ fav.city }}
                      </span>
                    }
                  </div>
                </div>
              </a>
              <button
                type="button"
                class="remove"
                [disabled]="removingId() === fav.eventId"
                (click)="remove(fav.eventId)"
                aria-label="Aus Favoriten entfernen"
              >
                <i-lucide [img]="TrashIcon" [size]="16" />
              </button>
            </li>
          }
        </ul>
      }
    </div>
  `,
  styles: `
    .page {
      max-width: 880px;
      margin: 0 auto;
      padding: 2rem 1.5rem;
    }
    .head {
      margin-bottom: 1.5rem;
    }
    .head h1 {
      font-size: 1.75rem;
      margin: 0 0 0.25rem;
    }
    .subtitle {
      color: var(--muted-foreground);
      font-size: 0.9375rem;
      margin: 0;
    }
    .state {
      text-align: center;
      color: var(--muted-foreground);
      padding: 4rem 0;
    }
    .empty,
    .error {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      padding: 4rem 0;
      gap: 0.5rem;
    }
    .empty-icon {
      color: var(--muted-foreground);
      margin-bottom: 0.5rem;
    }
    .error i-lucide {
      color: #ef4444;
    }
    .empty h3,
    .error h3 {
      margin: 0;
      font-size: 1.25rem;
    }
    .empty p,
    .error p {
      color: var(--muted-foreground);
      font-size: 0.9375rem;
      margin: 0 0 0.75rem;
    }
    .cta,
    .error button {
      background: var(--primary);
      color: var(--primary-foreground);
      border: none;
      border-radius: var(--radius);
      padding: 0.625rem 1.25rem;
      font-size: 0.875rem;
      cursor: pointer;
      text-decoration: none;
      display: inline-block;
    }
    .list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .row {
      display: flex;
      align-items: stretch;
      gap: 0.75rem;
      border: 1px solid var(--border);
      background: var(--card);
      border-radius: var(--radius);
      overflow: hidden;
      transition: border-color 0.15s;
    }
    .row:hover {
      border-color: color-mix(in srgb, var(--primary) 30%, transparent);
    }
    .row-link {
      display: flex;
      flex: 1;
      gap: 0.875rem;
      align-items: center;
      padding: 0.625rem;
      text-decoration: none;
      color: inherit;
      min-width: 0;
    }
    .row-link img {
      width: 96px;
      height: 64px;
      object-fit: cover;
      border-radius: calc(var(--radius) - 2px);
      flex-shrink: 0;
    }
    .info {
      min-width: 0;
      flex: 1;
    }
    .info h3 {
      margin: 0 0 0.25rem;
      font-size: 0.9375rem;
      font-weight: 600;
      line-height: 1.3;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .meta {
      display: flex;
      flex-wrap: wrap;
      gap: 0.75rem;
      color: var(--muted-foreground);
      font-size: 0.8125rem;
    }
    .meta span {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
    }
    .remove {
      background: transparent;
      color: var(--muted-foreground);
      border: none;
      padding: 0 0.875rem;
      cursor: pointer;
      transition: color 0.15s;
    }
    .remove:hover {
      color: #ef4444;
    }
    .remove:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `,
})
export class Favorites implements OnInit {
  private favoriteService = inject(FavoriteService);

  favorites = signal<Favorite[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  removingId = signal<string | null>(null);

  readonly HeartIcon = Heart;
  readonly TrashIcon = Trash2;
  readonly CalendarIcon = Calendar;
  readonly MapPinIcon = MapPin;
  readonly AlertIcon = AlertTriangle;
  readonly fallbackImage =
    "https://placehold.co/120x80/1a1a2e/e0e0e0?text=Kein+Bild";

  ngOnInit(): void {
    this.favoriteService.loadIds();
    this.load();
  }

  reload(): void {
    this.load();
  }

  remove(eventId: string): void {
    this.removingId.set(eventId);
    this.favoriteService.removeFavorite(eventId).subscribe({
      next: () => {
        this.favorites.update((list) =>
          list.filter((f) => f.eventId !== eventId),
        );
        this.removingId.set(null);
      },
      error: () => this.removingId.set(null),
    });
  }

  onImageError(event: globalThis.Event): void {
    const img = event.target as HTMLImageElement;
    img.src = this.fallbackImage;
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.favoriteService.getFavorites().subscribe({
      next: (favs) => {
        this.favorites.set(favs);
        this.loading.set(false);
      },
      error: () => {
        this.favorites.set([]);
        this.loading.set(false);
        this.error.set(
          "Favoriten konnten nicht geladen werden. Bitte versuche es später erneut.",
        );
      },
    });
  }
}
