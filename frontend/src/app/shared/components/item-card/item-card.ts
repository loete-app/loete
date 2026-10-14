import { Component, input } from "@angular/core";
import { RouterLink } from "@angular/router";
import { CurrencyPipe, UpperCasePipe } from "@angular/common";
import { SampleItem } from "@/core/models/item.model";

@Component({
  selector: "app-item-card",
  imports: [RouterLink, CurrencyPipe, UpperCasePipe],
  template: `
    <a [routerLink]="['/items', item().id]" class="card">
      <div class="row">
        <span class="badge category">{{ item().category }}</span>
        <span class="badge priority" [attr.data-priority]="item().priority">
          {{ item().priority | uppercase }}
        </span>
      </div>
      <h3>{{ item().title }}</h3>
      <p class="description">{{ item().description }}</p>
      <p class="price">{{ item().price | currency: "CHF" }}</p>
    </a>
  `,
  styles: `
    .card {
      display: block;
      background: var(--card);
      border-radius: var(--radius);
      padding: 1.25rem;
      text-decoration: none;
      color: inherit;
      transition: background 0.15s;
    }
    .card:hover {
      background: var(--accent);
    }
    h3 {
      font-size: 1.125rem;
      margin: 0.5rem 0 0.25rem;
    }
    .row {
      display: flex;
      gap: 0.5rem;
    }
    .badge {
      font-size: 0.75rem;
      background: var(--secondary);
      color: var(--muted-foreground);
      padding: 0.125rem 0.5rem;
      border-radius: 999px;
    }
    .badge.priority[data-priority="HIGH"],
    .badge.priority[data-priority="URGENT"] {
      color: var(--destructive);
    }
    .description {
      font-size: 0.875rem;
      color: var(--muted-foreground);
    }
    .price {
      font-weight: 600;
      color: var(--primary);
      margin-top: 0.25rem;
    }
  `,
})
export class ItemCard {
  item = input.required<SampleItem>();
}
