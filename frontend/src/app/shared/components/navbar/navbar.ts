import { Component } from "@angular/core";
import { RouterLink, RouterLinkActive } from "@angular/router";
import { LucideAngularModule, Heart } from "lucide-angular";

@Component({
  selector: "app-navbar",
  imports: [RouterLink, RouterLinkActive, LucideAngularModule],
  template: `
    <nav class="navbar">
      <a routerLink="/" class="brand">Löte</a>
      <div class="links">
        <a
          routerLink="/"
          routerLinkActive="active"
          [routerLinkActiveOptions]="{ exact: true }"
        >
          Events
        </a>
        <a routerLink="/favoriten" routerLinkActive="active" class="fav-link">
          <i-lucide [img]="HeartIcon" [size]="14" />
          Favoriten
        </a>
      </div>
    </nav>
  `,
  styles: `
    .navbar {
      height: 64px;
      display: flex;
      align-items: center;
      padding: 0 1.5rem;
      gap: 2rem;
      border-bottom: 1px solid var(--border);
    }
    .brand {
      font-family: "Figtree", system-ui, sans-serif;
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--primary);
      text-decoration: none;
    }
    .links {
      display: flex;
      gap: 1rem;
    }
    .links a {
      color: var(--muted-foreground);
      text-decoration: none;
      font-size: 0.875rem;
      transition: color 0.15s;
    }
    .fav-link {
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
    }
    .links a:hover,
    .links a.active {
      color: var(--foreground);
    }
  `,
})
export class Navbar {
  readonly HeartIcon = Heart;
}
