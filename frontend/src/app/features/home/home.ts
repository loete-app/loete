import { Component, inject, OnInit, signal } from "@angular/core";
import { ItemService } from "@/core/services/item.service";
import { SampleItem, ItemCategory, Priority } from "@/core/models/item.model";
import { ItemCard } from "@/shared/components/item-card/item-card";
import {
  FilterBar,
  FilterChange,
} from "@/shared/components/filter-bar/filter-bar";

@Component({
  selector: "app-home",
  imports: [ItemCard, FilterBar],
  template: `
    <section class="page">
      <h1>Items</h1>
      <app-filter-bar (filterChange)="onFilter($event)" />

      @if (loading()) {
        <p class="muted">Loading...</p>
      } @else if (items().length) {
        <div class="grid">
          @for (item of items(); track item.id) {
            <app-item-card [item]="item" />
          }
        </div>
      } @else {
        <p class="muted">No items found.</p>
      }
    </section>
  `,
  styles: `
    .page {
      max-width: 720px;
      margin: 0 auto;
      padding: 2rem 1.5rem;
    }
    h1 {
      margin-bottom: 1.5rem;
    }
    .grid {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .muted {
      color: var(--muted-foreground);
    }
  `,
})
export class Home implements OnInit {
  private itemService = inject(ItemService);

  items = signal<SampleItem[]>([]);
  loading = signal(true);

  private currentCategory: ItemCategory | undefined;
  private currentPriority: Priority | undefined;

  ngOnInit() {
    this.load();
  }

  onFilter(filters: FilterChange) {
    this.currentCategory = filters.category;
    this.currentPriority = filters.priority;
    this.load();
  }

  private load() {
    this.loading.set(true);
    this.itemService
      .getItems({
        category: this.currentCategory,
        priority: this.currentPriority,
      })
      .subscribe({
        next: (data) => {
          this.items.set(data);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
