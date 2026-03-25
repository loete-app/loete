import { Component, output } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { ItemCategory, Priority } from "@/core/models/item.model";

export interface FilterChange {
  category: ItemCategory | undefined;
  priority: Priority | undefined;
}

@Component({
  selector: "app-filter-bar",
  imports: [FormsModule],
  template: `
    <div class="bar">
      <select [(ngModel)]="selectedCategory" (ngModelChange)="emit()">
        <option [ngValue]="undefined">All categories</option>
        @for (cat of categories; track cat) {
          <option [ngValue]="cat">{{ cat }}</option>
        }
      </select>

      <select [(ngModel)]="selectedPriority" (ngModelChange)="emit()">
        <option [ngValue]="undefined">All priorities</option>
        @for (p of priorities; track p) {
          <option [ngValue]="p">{{ p }}</option>
        }
      </select>
    </div>
  `,
  styles: `
    .bar {
      display: flex;
      gap: 0.75rem;
      margin-bottom: 1.5rem;
    }
    select {
      background: var(--secondary);
      color: var(--foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.5rem 0.75rem;
      font-size: 0.875rem;
      cursor: pointer;
    }
    select:focus {
      outline: 2px solid var(--ring);
      outline-offset: 2px;
    }
  `,
})
export class FilterBar {
  filterChange = output<FilterChange>();

  categories: ItemCategory[] = [
    "BOOK",
    "ELECTRONICS",
    "CLOTHING",
    "FOOD",
    "OTHER",
  ];
  priorities: Priority[] = ["LOW", "MEDIUM", "HIGH", "URGENT"];

  selectedCategory: ItemCategory | undefined;
  selectedPriority: Priority | undefined;

  emit() {
    this.filterChange.emit({
      category: this.selectedCategory,
      priority: this.selectedPriority,
    });
  }
}
