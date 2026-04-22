import {
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from "@angular/core";
import { FormsModule } from "@angular/forms";
import { CategoryService } from "@/core/services/category.service";
import { LocationService } from "@/core/services/location.service";
import { Category } from "@/core/models/category.model";
import { LucideAngularModule, Search, X } from "lucide-angular";

export interface FilterValues {
  search: string;
  categoryId: number | null;
  city: string | null;
  dateFrom: string | null;
  dateTo: string | null;
}

@Component({
  selector: "app-filter-bar",
  imports: [FormsModule, LucideAngularModule],
  template: `
    <div class="filter-bar">
      <div class="search-wrap">
        <i-lucide [img]="SearchIcon" [size]="16" class="search-icon" />
        <input
          type="search"
          [(ngModel)]="search"
          (ngModelChange)="onSearchInput()"
          placeholder="Eventname suchen..."
          aria-label="Eventname suchen"
        />
      </div>

      <div class="filters">
        <select
          [(ngModel)]="categoryId"
          (ngModelChange)="onChange()"
          aria-label="Kategorie"
        >
          <option [ngValue]="null">Alle Kategorien</option>
          @for (cat of categories(); track cat.id) {
            <option [ngValue]="cat.id">{{ cat.name }}</option>
          }
        </select>

        <select
          [(ngModel)]="city"
          (ngModelChange)="onChange()"
          aria-label="Ort"
        >
          <option [ngValue]="null">Alle Orte</option>
          @for (c of cities(); track c) {
            <option [ngValue]="c">{{ c }}</option>
          }
        </select>

        <input
          type="date"
          [(ngModel)]="dateFrom"
          (ngModelChange)="onChange()"
          aria-label="Startdatum"
        />
        <input
          type="date"
          [(ngModel)]="dateTo"
          (ngModelChange)="onChange()"
          aria-label="Enddatum"
        />

        @if (hasActiveFilters()) {
          <button
            type="button"
            class="reset"
            (click)="reset()"
            aria-label="Filter zurücksetzen"
          >
            <i-lucide [img]="XIcon" [size]="14" />
            Zurücksetzen
          </button>
        }
      </div>
    </div>
  `,
  styles: `
    .filter-bar {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-bottom: 1.5rem;
    }
    .search-wrap {
      position: relative;
    }
    .search-icon {
      position: absolute;
      left: 0.875rem;
      top: 50%;
      transform: translateY(-50%);
      color: var(--muted-foreground);
      pointer-events: none;
    }
    .search-wrap input {
      width: 100%;
      padding: 0.625rem 1rem 0.625rem 2.5rem;
      background: var(--card);
      color: var(--foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      font-size: 0.9375rem;
      outline: none;
      transition: border-color 0.15s;
    }
    .search-wrap input:focus {
      border-color: var(--primary);
    }
    .filters {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      align-items: center;
    }
    select,
    input[type="date"] {
      background: var(--card);
      color: var(--foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.5rem 0.75rem;
      font-size: 0.875rem;
      outline: none;
      cursor: pointer;
      transition: border-color 0.15s;
      min-width: 0;
    }
    select:focus,
    input[type="date"]:focus {
      border-color: var(--primary);
    }
    select {
      min-width: 160px;
    }
    .reset {
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
      background: transparent;
      color: var(--muted-foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.5rem 0.75rem;
      font-size: 0.8125rem;
      cursor: pointer;
      transition:
        color 0.15s,
        border-color 0.15s;
    }
    .reset:hover {
      color: var(--foreground);
      border-color: var(--primary);
    }
  `,
})
export class FilterBar implements OnInit {
  private categoryService = inject(CategoryService);
  private locationService = inject(LocationService);

  initial = input<FilterValues | null>(null);
  filtersChange = output<FilterValues>();

  categories = signal<Category[]>([]);
  cities = signal<string[]>([]);

  search = "";
  categoryId: number | null = null;
  city: string | null = null;
  dateFrom: string | null = null;
  dateTo: string | null = null;

  readonly SearchIcon = Search;
  readonly XIcon = X;

  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    const init = this.initial();
    if (init) {
      this.search = init.search ?? "";
      this.categoryId = init.categoryId ?? null;
      this.city = init.city ?? null;
      this.dateFrom = init.dateFrom ?? null;
      this.dateTo = init.dateTo ?? null;
    }

    this.categoryService.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
      error: () => this.categories.set([]),
    });
    this.locationService.getCities().subscribe({
      next: (cities) => this.cities.set(cities),
      error: () => this.cities.set([]),
    });
  }

  onSearchInput(): void {
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => this.onChange(), 300);
  }

  onChange(): void {
    this.filtersChange.emit({
      search: this.search.trim(),
      categoryId: this.categoryId,
      city: this.city,
      dateFrom: this.dateFrom,
      dateTo: this.dateTo,
    });
  }

  reset(): void {
    this.search = "";
    this.categoryId = null;
    this.city = null;
    this.dateFrom = null;
    this.dateTo = null;
    this.onChange();
  }

  hasActiveFilters(): boolean {
    return !!(
      this.search ||
      this.categoryId != null ||
      this.city ||
      this.dateFrom ||
      this.dateTo
    );
  }
}
