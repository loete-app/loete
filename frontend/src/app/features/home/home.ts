/**
 * Startseite der Loete-Anwendung.
 *
 * Zeigt eine gefilterte, paginierte Event-Liste an. Unterstützt
 * Kategorie-, Stadt- und Datumsfilter sowie semantische Vibe-Suche.
 * Filter werden als URL-Query-Parameter synchronisiert.
 */
import { Component, inject, OnInit, signal } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { EventService } from "@/core/services/event.service";
import { VibeSearchService } from "@/core/services/vibe-search.service";
import { FavoriteService } from "@/core/services/favorite.service";
import { SeoService } from "@/core/services/seo.service";
import { Event, EventFilter } from "@/core/models/event.model";
import { EventCard } from "@/shared/components/event-card/event-card";
import {
  FilterBar,
  FilterValues,
} from "@/shared/components/filter-bar/filter-bar";
import { LucideAngularModule, CalendarX2, AlertTriangle } from "lucide-angular";

@Component({
  selector: "app-home",
  imports: [EventCard, FilterBar, LucideAngularModule],
  template: `
    <div class="page">
      <section class="hero">
        <h1>Entdecke Events</h1>
        <p class="subtitle">
          Finde Konzerte, Sport, Theater und mehr in deiner Nahe.
        </p>
      </section>

      <app-filter-bar
        [initial]="initialFilters()"
        searchPlaceholder="Suche nach Vibe, Stimmung oder Eventname..."
        (filtersChange)="onFiltersChange($event)"
      />

      @if (loading()) {
        <div class="loading">
          <p>Events werden geladen...</p>
        </div>
      } @else if (error()) {
        <div class="error">
          <i-lucide [img]="AlertIcon" [size]="40" class="error-icon" />
          <h3>Events konnten nicht geladen werden</h3>
          <p>{{ error() }}</p>
          <button (click)="reload()">Erneut versuchen</button>
        </div>
      } @else if (events().length === 0) {
        <div class="empty">
          <i-lucide [img]="CalendarX2Icon" [size]="48" class="empty-icon" />
          <h3>Keine Events gefunden</h3>
          <p>Passe deine Filter an oder schau spater nochmal vorbei.</p>
        </div>
      } @else {
        <div class="grid">
          @for (event of events(); track event.id) {
            <app-event-card
              [event]="event"
              class="animate-fade-in-up"
              [style.animation-delay]="$index * 50 + 'ms'"
            />
          }
        </div>

        @if (!isSearchActive() && !lastPage()) {
          <div class="load-more">
            <button (click)="loadMore()" [disabled]="loadingMore()">
              {{ loadingMore() ? "Laden..." : "Mehr Events laden" }}
            </button>
          </div>
        }
      }
    </div>
  `,
  styles: `
    .page {
      max-width: 1280px;
      margin: 0 auto;
      padding: 2rem 1.5rem;
    }
    .hero {
      margin-bottom: 1.5rem;
    }
    .hero h1 {
      font-size: 2rem;
      margin: 0 0 0.5rem;
    }
    .subtitle {
      color: var(--muted-foreground);
      font-size: 1rem;
      margin: 0;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(1, 1fr);
      gap: 1.5rem;
    }
    @media (min-width: 640px) {
      .grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }
    @media (min-width: 1024px) {
      .grid {
        grid-template-columns: repeat(3, 1fr);
      }
    }
    @media (min-width: 1280px) {
      .grid {
        grid-template-columns: repeat(4, 1fr);
      }
    }
    .loading {
      display: flex;
      justify-content: center;
      padding: 6rem 0;
      color: var(--muted-foreground);
    }
    .empty,
    .error {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      padding: 6rem 0;
    }
    .empty-icon,
    .error-icon {
      color: var(--muted-foreground);
      margin-bottom: 1rem;
    }
    .error-icon {
      color: #ef4444;
    }
    .empty h3,
    .error h3 {
      font-size: 1.25rem;
      margin: 0 0 0.25rem;
    }
    .empty p,
    .error p {
      color: var(--muted-foreground);
      font-size: 0.875rem;
      margin: 0 0 1rem;
    }
    .error button,
    .load-more button {
      background: var(--secondary);
      color: var(--foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.625rem 1.5rem;
      font-size: 0.875rem;
      cursor: pointer;
      transition: background 0.15s;
    }
    .error button:hover,
    .load-more button:hover:not(:disabled) {
      background: var(--accent);
    }
    .load-more {
      display: flex;
      justify-content: center;
      margin-top: 2rem;
    }
    .load-more button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `,
})
export class Home implements OnInit {
  /** Service für Event-Abfragen. */
  private eventService = inject(EventService);
  /** Service für die Vibe-Suche. */
  private vibeSearchService = inject(VibeSearchService);
  /** Service für Favoriten-Initialisierung. */
  private favoriteService = inject(FavoriteService);
  /** Aktive Route für Query-Parameter. */
  private route = inject(ActivatedRoute);
  /** Router für Query-Parameter-Synchronisation. */
  private router = inject(Router);
  /** Service für SEO-Meta-Tags. */
  private seo = inject(SeoService);

  /** Icon für den leeren Zustand (keine Events). */
  readonly CalendarX2Icon = CalendarX2;
  /** Icon für Fehlermeldungen. */
  readonly AlertIcon = AlertTriangle;

  /** Liste der angezeigten Events. */
  events = signal<Event[]>([]);
  /** Ladezustand beim initialen Laden. */
  loading = signal(true);
  /** Ladezustand beim Nachladen weiterer Events. */
  loadingMore = signal(false);
  /** Ob die letzte Seite erreicht ist. */
  lastPage = signal(false);
  /** Fehlermeldung bei Ladeproblemen. */
  error = signal<string | null>(null);
  /** Initiale Filterwerte aus den URL-Query-Parametern. */
  initialFilters = signal<FilterValues | null>(null);

  /** Aktuelle Seitennummer für die Paginierung. */
  private currentPage = 0;
  /** Aktuelle Filterwerte. */
  private filters: FilterValues = {
    search: "",
    categoryId: null,
    city: null,
    dateFrom: null,
    dateTo: null,
  };

  /** Initialisiert die Seite, liest URL-Filter und lädt Events. */
  ngOnInit(): void {
    this.seo.set(
      "Events entdecken",
      "Entdecke Konzerte, Festivals, Sport und mehr in der Schweiz - an einem Ort.",
    );
    this.favoriteService.init();

    const qp = this.route.snapshot.queryParamMap;
    this.filters = {
      search: qp.get("search") ?? "",
      categoryId: qp.get("categoryId") ? Number(qp.get("categoryId")) : null,
      city: qp.get("city"),
      dateFrom: qp.get("dateFrom"),
      dateTo: qp.get("dateTo"),
    };
    this.initialFilters.set({ ...this.filters });

    this.loadEvents();
  }

  /** Reagiert auf Filteraenderungen aus der FilterBar. */
  onFiltersChange(values: FilterValues): void {
    this.filters = values;
    this.currentPage = 0;
    this.syncQueryParams();
    this.loadEvents();
  }

  /** Prüft, ob aktuell eine Textsuche aktiv ist. */
  isSearchActive(): boolean {
    return !!this.filters.search;
  }

  /** Lädt die nächste Seite an Events nach. */
  loadMore(): void {
    this.currentPage++;
    this.loadingMore.set(true);
    this.eventService
      .getEvents({ ...this.toApiFilter(), page: this.currentPage })
      .subscribe({
        next: (res) => {
          this.events.update((prev) => [...prev, ...res.content]);
          this.lastPage.set(res.last);
          this.loadingMore.set(false);
        },
        error: () => this.loadingMore.set(false),
      });
  }

  /** Setzt die Paginierung zurück und lädt Events erneut. */
  reload(): void {
    this.currentPage = 0;
    this.loadEvents();
  }

  /** Lädt Events via Browse oder Vibe-Suche je nach Filterung. */
  private loadEvents(): void {
    this.loading.set(true);
    this.error.set(null);

    if (this.filters.search) {
      this.loadVibeResults();
    } else {
      this.loadBrowseResults();
    }
  }

  /** Lädt paginierte Browse-Ergebnisse vom Event-Service. */
  private loadBrowseResults(): void {
    this.eventService
      .getEvents({ ...this.toApiFilter(), page: this.currentPage })
      .subscribe({
        next: (res) => {
          this.events.set(res.content);
          this.lastPage.set(res.last);
          this.loading.set(false);
        },
        error: () => {
          this.events.set([]);
          this.error.set(
            "Wir konnten gerade keine Events laden. Bitte versuche es in wenigen Augenblicken erneut.",
          );
          this.loading.set(false);
        },
      });
  }

  /** Lädt Vibe-Suchergebnisse vom VibeSearch-Service. */
  private loadVibeResults(): void {
    this.vibeSearchService
      .search({
        query: this.filters.search,
        categoryId: this.filters.categoryId,
        city: this.filters.city,
        dateFrom: this.filters.dateFrom
          ? `${this.filters.dateFrom}T00:00:00`
          : null,
        dateTo: this.filters.dateTo ? `${this.filters.dateTo}T23:59:59` : null,
      })
      .subscribe({
        next: (res) => {
          this.events.set(res.results);
          this.lastPage.set(true);
          this.loading.set(false);
        },
        error: () => {
          this.events.set([]);
          this.error.set(
            "Wir konnten gerade keine Events laden. Bitte versuche es in wenigen Augenblicken erneut.",
          );
          this.loading.set(false);
        },
      });
  }

  /** Konvertiert die aktuellen Filter in ein API-kompatibles Format. */
  private toApiFilter(): EventFilter {
    return {
      search: this.filters.search || null,
      categoryId: this.filters.categoryId,
      city: this.filters.city,
      dateFrom: this.filters.dateFrom
        ? `${this.filters.dateFrom}T00:00:00`
        : null,
      dateTo: this.filters.dateTo ? `${this.filters.dateTo}T23:59:59` : null,
    };
  }

  /** Synchronisiert die aktuellen Filter als URL-Query-Parameter. */
  private syncQueryParams(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        search: this.filters.search || null,
        categoryId: this.filters.categoryId ?? null,
        city: this.filters.city || null,
        dateFrom: this.filters.dateFrom || null,
        dateTo: this.filters.dateTo || null,
      },
      queryParamsHandling: "merge",
      replaceUrl: true,
    });
  }
}
