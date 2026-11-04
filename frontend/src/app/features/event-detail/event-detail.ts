import { Component, inject, OnInit, signal } from "@angular/core";
import { DatePipe, Location } from "@angular/common";
import { ActivatedRoute } from "@angular/router";
import { EventService } from "@/core/services/event.service";
import { FavoriteService } from "@/core/services/favorite.service";
import { SeoService } from "@/core/services/seo.service";
import { EventDetail } from "@/core/models/event.model";
import {
  LucideAngularModule,
  Calendar,
  MapPin,
  ArrowLeft,
  Heart,
  Ticket,
  AlertTriangle,
} from "lucide-angular";

@Component({
  selector: "app-event-detail",
  imports: [DatePipe, LucideAngularModule],
  template: `
    <div class="page">
      <button class="back" type="button" (click)="goBack()">
        <i-lucide [img]="BackIcon" [size]="16" />
        Zurück
      </button>

      @if (loading()) {
        <p class="state">Event wird geladen...</p>
      } @else if (error()) {
        <div class="error">
          <i-lucide [img]="AlertIcon" [size]="40" />
          <h2>Event konnte nicht geladen werden</h2>
          <p>{{ error() }}</p>
        </div>
      } @else if (event(); as ev) {
        <article class="event">
          <div class="hero">
            <img
              [src]="ev.imageUrl || fallbackImage"
              [alt]="ev.name"
              (error)="onImageError($event)"
            />
            @if (ev.categoryName) {
              <span class="badge">{{ ev.categoryName }}</span>
            }
          </div>

          <div class="content">
            <header>
              <h1>{{ ev.name }}</h1>
              <button
                type="button"
                class="fav"
                [class.active]="ev.favorited"
                [disabled]="favLoading()"
                (click)="toggleFavorite()"
                [attr.aria-pressed]="ev.favorited"
                [attr.aria-label]="
                  ev.favorited
                    ? 'Aus Favoriten entfernen'
                    : 'Als Favorit speichern'
                "
              >
                <i-lucide
                  [img]="HeartIcon"
                  [size]="18"
                  [attr.fill]="ev.favorited ? 'currentColor' : 'none'"
                />
                {{ ev.favorited ? "Favorit" : "Merken" }}
              </button>
            </header>

            <div class="meta">
              <div class="meta-item">
                <i-lucide [img]="CalendarIcon" [size]="16" />
                <div>
                  <div>{{ ev.startDate | date: "EEEE, d. MMMM y" }}</div>
                  <div class="muted">
                    {{ ev.startDate | date: "HH:mm" }} Uhr
                    @if (ev.endDate) {
                      – {{ ev.endDate | date: "HH:mm" }} Uhr
                    }
                  </div>
                </div>
              </div>
              @if (ev.locationName) {
                <div class="meta-item">
                  <i-lucide [img]="MapPinIcon" [size]="16" />
                  <div>
                    <div>{{ ev.locationName }}</div>
                    @if (ev.city) {
                      <div class="muted">
                        {{ ev.city }}
                        @if (ev.country) {
                          , {{ ev.country }}
                        }
                      </div>
                    }
                  </div>
                </div>
              }
            </div>

            @if (ev.description) {
              <p class="description">{{ ev.description }}</p>
            }

            @if (ev.ticketUrl && isSafeUrl(ev.ticketUrl)) {
              <a
                class="ticket-btn"
                [href]="ev.ticketUrl"
                target="_blank"
                rel="noopener noreferrer"
              >
                <i-lucide [img]="TicketIcon" [size]="18" />
                Tickets kaufen
              </a>
            }
          </div>
        </article>
      }
    </div>
  `,
  styles: `
    .page {
      max-width: 960px;
      margin: 0 auto;
      padding: 1.5rem;
    }
    .back {
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
      background: transparent;
      color: var(--muted-foreground);
      border: none;
      padding: 0.5rem 0;
      margin-bottom: 1rem;
      font-size: 0.875rem;
      cursor: pointer;
    }
    .back:hover {
      color: var(--foreground);
    }
    .state {
      color: var(--muted-foreground);
      text-align: center;
      padding: 4rem 0;
    }
    .error {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      gap: 0.5rem;
      padding: 4rem 0;
      color: var(--muted-foreground);
    }
    .error i-lucide {
      color: #ef4444;
    }
    .event {
      border-radius: var(--radius);
      border: 1px solid var(--border);
      background: var(--card);
      overflow: hidden;
    }
    .hero {
      position: relative;
      aspect-ratio: 16 / 9;
      max-height: 420px;
      overflow: hidden;
    }
    .hero img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .badge {
      position: absolute;
      top: 1rem;
      left: 1rem;
      font-size: 0.75rem;
      font-weight: 500;
      background: var(--primary);
      color: var(--primary-foreground);
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
    }
    .content {
      padding: 1.5rem;
    }
    header {
      display: flex;
      align-items: flex-start;
      gap: 1rem;
      margin-bottom: 1rem;
    }
    h1 {
      font-size: 1.75rem;
      margin: 0;
      flex: 1;
      line-height: 1.2;
    }
    .fav {
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
      background: var(--secondary);
      color: var(--foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.5rem 0.875rem;
      font-size: 0.875rem;
      cursor: pointer;
      transition:
        color 0.15s,
        background 0.15s;
      flex-shrink: 0;
    }
    .fav:hover {
      background: var(--accent);
    }
    .fav.active {
      color: #ef4444;
      border-color: color-mix(in srgb, #ef4444 30%, transparent);
    }
    .fav:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .meta {
      display: flex;
      flex-direction: column;
      gap: 0.875rem;
      margin-bottom: 1.25rem;
    }
    .meta-item {
      display: flex;
      align-items: flex-start;
      gap: 0.625rem;
      font-size: 0.9375rem;
    }
    .meta-item i-lucide {
      color: color-mix(in srgb, var(--primary) 70%, transparent);
      margin-top: 0.125rem;
      flex-shrink: 0;
    }
    .muted {
      color: var(--muted-foreground);
      font-size: 0.875rem;
    }
    .description {
      font-size: 0.9375rem;
      line-height: 1.6;
      color: var(--foreground);
      margin: 0 0 1.5rem;
      white-space: pre-line;
    }
    .ticket-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: var(--primary);
      color: var(--primary-foreground);
      border-radius: var(--radius);
      padding: 0.75rem 1.25rem;
      font-size: 0.9375rem;
      font-weight: 600;
      text-decoration: none;
      transition: opacity 0.15s;
    }
    .ticket-btn:hover {
      opacity: 0.9;
    }
  `,
})
export class EventDetailPage implements OnInit {
  private route = inject(ActivatedRoute);
  private location = inject(Location);
  private eventService = inject(EventService);
  private favoriteService = inject(FavoriteService);
  private seo = inject(SeoService);

  event = signal<EventDetail | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  favLoading = signal(false);

  readonly CalendarIcon = Calendar;
  readonly MapPinIcon = MapPin;
  readonly BackIcon = ArrowLeft;
  readonly HeartIcon = Heart;
  readonly TicketIcon = Ticket;
  readonly AlertIcon = AlertTriangle;
  readonly fallbackImage =
    "https://placehold.co/960x500/1a1a2e/e0e0e0?text=Kein+Bild";

  ngOnInit(): void {
    this.seo.set("Event-Details", "Event-Details auf Löte.");
    this.favoriteService.init();
    this.route.paramMap.subscribe((params) => {
      const id = params.get("id");
      if (id) this.loadEvent(id);
    });
  }

  goBack(): void {
    this.location.back();
  }

  isSafeUrl(url: string): boolean {
    try {
      const p = new URL(url);
      return p.protocol === "https:" || p.protocol === "http:";
    } catch {
      return false;
    }
  }

  toggleFavorite(): void {
    const ev = this.event();
    if (!ev || this.favLoading()) return;

    this.favLoading.set(true);

    const handlers = {
      next: () => {
        this.event.set({ ...ev, favorited: !ev.favorited });
        this.favLoading.set(false);
      },
      error: () => this.favLoading.set(false),
    };

    if (ev.favorited) {
      this.favoriteService.removeFavorite(ev.id).subscribe(handlers);
    } else {
      this.favoriteService.addFavorite(ev.id).subscribe(handlers);
    }
  }

  onImageError(event: globalThis.Event): void {
    const img = event.target as HTMLImageElement;
    img.src = this.fallbackImage;
  }

  private loadEvent(id: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.eventService.getEvent(id).subscribe({
      next: (ev) => {
        this.event.set(ev);
        this.loading.set(false);
        const desc = ev.description?.trim()
          ? ev.description.trim().slice(0, 160)
          : `${ev.name}${ev.city ? ` – ${ev.city}` : ""}. Tickets und Details auf Löte.`;
        this.seo.set(ev.name, desc);
        window.scrollTo({ top: 0 });
      },
      error: (err) => {
        this.loading.set(false);
        if (err?.status === 404) {
          this.error.set("Dieses Event existiert nicht (mehr).");
          this.seo.set("Event nicht gefunden", "Dieses Event existiert nicht.");
        } else {
          this.error.set(
            "Wir konnten dieses Event gerade nicht laden. Bitte versuche es später erneut.",
          );
        }
      },
    });
  }
}
