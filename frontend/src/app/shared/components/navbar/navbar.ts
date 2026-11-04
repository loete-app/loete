import { Component, inject } from "@angular/core";
import { RouterLink, RouterLinkActive } from "@angular/router";
import { LucideAngularModule, Heart } from "lucide-angular";
import { AuthService } from "@/core/services/auth.service";

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
      <div class="auth">
        @if (currentUser(); as user) {
          <span class="username">{{ user.username }}</span>
          <button type="button" class="logout-btn" (click)="onLogout()">
            Abmelden
          </button>
        } @else {
          <a routerLink="/login" class="auth-link">Anmelden</a>
          <a routerLink="/registrieren" class="register-link">Registrieren</a>
        }
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
      flex: 1;
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
    .auth {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
    .username {
      font-size: 0.8125rem;
      color: var(--muted-foreground);
    }
    .logout-btn {
      background: transparent;
      color: var(--muted-foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.375rem 0.75rem;
      font-size: 0.8125rem;
      cursor: pointer;
      transition:
        color 0.15s,
        border-color 0.15s;
    }
    .logout-btn:hover {
      color: var(--foreground);
      border-color: var(--foreground);
    }
    .auth-link,
    .register-link {
      font-size: 0.8125rem;
      text-decoration: none;
      transition: color 0.15s;
    }
    .auth-link {
      color: var(--muted-foreground);
    }
    .auth-link:hover {
      color: var(--foreground);
    }
    .register-link {
      background: var(--primary);
      color: var(--primary-foreground);
      border-radius: var(--radius);
      padding: 0.375rem 0.75rem;
      font-weight: 500;
    }
    .register-link:hover {
      opacity: 0.9;
    }
  `,
})
export class Navbar {
  private authService = inject(AuthService);
  readonly HeartIcon = Heart;
  readonly currentUser = this.authService.currentUser;

  onLogout(): void {
    this.authService.logout();
  }
}
