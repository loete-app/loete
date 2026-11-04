import { Component, inject, input, signal } from "@angular/core";
import { DatePipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import { Event } from "@/core/models/event.model";
import { FavoriteService } from "@/core/services/favorite.service";
import { LucideAngularModule, Calendar, MapPin, Heart } from "lucide-angular";

@Component({
  selector: "app-event-card",
  imports: [DatePipe, RouterLink, LucideAngularModule],
  template: `
    <article class="card">
      <a [routerLink]="['/events', event().id]" class="card-link">
        <div class="image-wrapper">
          <img
            [src]="event().imageUrl || fallbackImage"
            [alt]="event().name"
            (error)="onImageError($event)"
            loading="lazy"
          />
          @if (event().categoryName) {
            <span class="badge">{{ event().categoryName }}</span>
          }
        </div>
        <div class="body">
          <h3>{{ event().name }}</h3>
          <div class="meta">
            <span class="meta-item">
              <i-lucide [img]="CalendarIcon" [size]="14" />
              {{ event().startDate | date: "d. MMM y" }}
            </span>
            @if (event().city) {
              <span class="meta-item">
                <i-lucide [img]="MapPinIcon" [size]="14" />
                {{ event().city }}
              </span>
            }
          </div>
        </div>
      </a>
      <button
        type="button"
        class="fav-btn"
        [class.active]="isFavorite()"
        [disabled]="busy()"
        (click)="toggleFavorite($event)"
        [attr.aria-label]="
          isFavorite() ? 'Aus Favoriten entfernen' : 'Als Favorit speichern'
        "
        [attr.aria-pressed]="isFavorite()"
      >
        <i-lucide
          [img]="HeartIcon"
          [size]="16"
          [attr.fill]="isFavorite() ? 'currentColor' : 'none'"
        />
      </button>
    </article>
  `,
  styles: `
    .card {
      position: relative;
      display: block;
      overflow: hidden;
      border-radius: var(--radius);
      border: 1px solid var(--border);
      background: var(--card);
      color: var(--card-foreground);
      transition: all 0.3s;
    }
    .card-link {
      display: block;
      color: inherit;
      text-decoration: none;
    }
    .card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
      border-color: color-mix(in srgb, var(--primary) 20%, transparent);
    }
    .image-wrapper {
      position: relative;
      aspect-ratio: 16 / 10;
      overflow: hidden;
    }
    .image-wrapper img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s;
    }
    .card:hover .image-wrapper img {
      transform: scale(1.05);
    }
    .badge {
      position: absolute;
      top: 0.75rem;
      left: 0.75rem;
      font-size: 0.75rem;
      font-weight: 500;
      background: var(--primary);
      color: var(--primary-foreground);
      padding: 0.125rem 0.625rem;
      border-radius: 999px;
    }
    .body {
      padding: 1rem;
    }
    h3 {
      font-size: 1rem;
      font-weight: 600;
      line-height: 1.4;
      margin: 0 0 0.5rem;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .meta {
      display: flex;
      flex-direction: column;
      gap: 0.375rem;
    }
    .meta-item {
      display: flex;
      align-items: center;
      gap: 0.375rem;
      font-size: 0.8125rem;
      color: var(--muted-foreground);
    }
    .meta-item i-lucide {
      color: color-mix(in srgb, var(--primary) 70%, transparent);
      flex-shrink: 0;
    }
    .fav-btn {
      position: absolute;
      top: 0.75rem;
      right: 0.75rem;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 2rem;
      height: 2rem;
      border-radius: 999px;
      border: none;
      background: rgba(0, 0, 0, 0.55);
      color: white;
      backdrop-filter: blur(6px);
      cursor: pointer;
      transition:
        transform 0.15s,
        background 0.15s,
        color 0.15s;
    }
    .fav-btn:hover:not(:disabled) {
      transform: scale(1.08);
    }
    .fav-btn.active {
      color: #ef4444;
    }
    .fav-btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  `,
})
export class EventCard {
  event = input.required<Event>();

  private favoriteService = inject(FavoriteService);

  readonly CalendarIcon = Calendar;
  readonly MapPinIcon = MapPin;
  readonly HeartIcon = Heart;
  readonly fallbackImage =
    "https://placehold.co/400x250/1a1a2e/e0e0e0?text=Kein+Bild";

  busy = signal(false);

  isFavorite(): boolean {
    return this.favoriteService.favoriteIds().has(this.event().id);
  }

  toggleFavorite(ev: MouseEvent): void {
    ev.preventDefault();
    ev.stopPropagation();
    if (this.busy()) return;

    this.busy.set(true);

    const id = this.event().id;
    const done = () => this.busy.set(false);
    const handlers = { next: done, error: done };

    if (this.isFavorite()) {
      this.favoriteService.removeFavorite(id).subscribe(handlers);
    } else {
      this.favoriteService.addFavorite(id).subscribe(handlers);
    }
  }

  onImageError(event: globalThis.Event): void {
    const img = event.target as HTMLImageElement;
    img.src = this.fallbackImage;
  }
}
