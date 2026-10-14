import { Component, inject, OnInit, signal } from "@angular/core";
import { EventService } from "@/core/services/event.service";
import { Event } from "@/core/models/event.model";
import { EventCard } from "@/shared/components/event-card/event-card";
import { LucideAngularModule, CalendarX2 } from "lucide-angular";

@Component({
  selector: "app-home",
  imports: [EventCard, LucideAngularModule],
  template: `
    <div class="page">
      <section class="hero">
        <h1>Entdecke Events</h1>
        <p class="subtitle">
          Finde Konzerte, Sport, Theater und mehr in deiner Nähe.
        </p>
      </section>

      @if (loading()) {
        <div class="loading">
          <p>Events werden geladen...</p>
        </div>
      } @else if (events().length === 0) {
        <div class="empty">
          <i-lucide [img]="CalendarX2Icon" [size]="48" class="empty-icon" />
          <h3>Keine Events gefunden</h3>
          <p>Schau später nochmal vorbei.</p>
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

        @if (!lastPage()) {
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
      margin-bottom: 2rem;
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
    .empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      padding: 6rem 0;
    }
    .empty-icon {
      color: var(--muted-foreground);
      margin-bottom: 1rem;
    }
    .empty h3 {
      font-size: 1.25rem;
      margin: 0 0 0.25rem;
    }
    .empty p {
      color: var(--muted-foreground);
      font-size: 0.875rem;
      margin: 0;
    }
    .load-more {
      display: flex;
      justify-content: center;
      margin-top: 2rem;
    }
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
    .load-more button:hover:not(:disabled) {
      background: var(--accent);
    }
    .load-more button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `,
})
export class Home implements OnInit {
  private eventService = inject(EventService);

  readonly CalendarX2Icon = CalendarX2;

  events = signal<Event[]>([]);
  loading = signal(true);
  loadingMore = signal(false);
  lastPage = signal(false);

  private currentPage = 0;

  ngOnInit() {
    this.loadEvents();
  }

  loadMore() {
    this.currentPage++;
    this.loadingMore.set(true);
    this.eventService.getEvents({ page: this.currentPage }).subscribe({
      next: (response) => {
        this.events.update((prev) => [...prev, ...response.content]);
        this.lastPage.set(response.last);
        this.loadingMore.set(false);
      },
      error: () => this.loadingMore.set(false),
    });
  }

  private loadEvents() {
    this.loading.set(true);
    this.eventService.getEvents({ page: this.currentPage }).subscribe({
      next: (response) => {
        this.events.set(response.content);
        this.lastPage.set(response.last);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
