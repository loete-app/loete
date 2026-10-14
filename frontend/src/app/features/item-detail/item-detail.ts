import { Component, inject, OnInit, signal } from "@angular/core";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { CurrencyPipe, UpperCasePipe, DatePipe } from "@angular/common";
import { ItemService } from "@/core/services/item.service";
import { SampleItem } from "@/core/models/item.model";
import { TruncatePipe } from "@/shared/pipes/truncate.pipe";

@Component({
  selector: "app-item-detail",
  imports: [RouterLink, CurrencyPipe, UpperCasePipe, DatePipe, TruncatePipe],
  template: `
    @if (loading()) {
      <section class="page"><p class="muted">Loading...</p></section>
    } @else if (item(); as item) {
      <section class="page">
        <a routerLink="/" class="back">&larr; Back</a>

        <div class="detail">
          <div class="badges">
            <span class="badge">{{ item.category }}</span>
            <span class="badge" [attr.data-priority]="item.priority">
              {{ item.priority | uppercase }}
            </span>
          </div>

          <h1>{{ item.title }}</h1>
          <p class="description">{{ item.description | truncate: 200 }}</p>
          <p class="price">{{ item.price | currency: "CHF" }}</p>
          <p class="meta">
            Created: {{ item.createdAt | date: "dd. MMM yyyy, HH:mm" }}
          </p>
        </div>
      </section>
    } @else {
      <section class="page"><p class="muted">Item not found.</p></section>
    }
  `,
  styles: `
    .page {
      max-width: 720px;
      margin: 0 auto;
      padding: 2rem 1.5rem;
    }
    .back {
      color: var(--muted-foreground);
      text-decoration: none;
      font-size: 0.875rem;
    }
    .back:hover {
      color: var(--foreground);
    }
    .detail {
      margin-top: 1.5rem;
      background: var(--card);
      border-radius: var(--radius);
      padding: 2rem;
    }
    .badges {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }
    .badge {
      font-size: 0.75rem;
      background: var(--secondary);
      color: var(--muted-foreground);
      padding: 0.125rem 0.5rem;
      border-radius: 999px;
    }
    .badge[data-priority="HIGH"],
    .badge[data-priority="URGENT"] {
      color: var(--destructive);
    }
    .description {
      color: var(--muted-foreground);
      margin: 0.75rem 0;
    }
    .price {
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--primary);
    }
    .meta {
      font-size: 0.8rem;
      color: var(--muted-foreground);
      margin-top: 1rem;
    }
    .muted {
      color: var(--muted-foreground);
    }
  `,
})
export class ItemDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private itemService = inject(ItemService);

  item = signal<SampleItem | null>(null);
  loading = signal(true);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get("id");
    if (id) {
      this.itemService.getItem(id).subscribe({
        next: (data) => {
          this.item.set(data);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    }
  }
}
