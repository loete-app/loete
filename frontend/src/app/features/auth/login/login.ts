import { Component, inject, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { AuthService } from "@/core/services/auth.service";
import { LoginRequest } from "@/core/models/auth.model";

@Component({
  selector: "app-login",
  imports: [FormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="card">
        <h1>Anmelden</h1>
        <p class="subtitle">Melde dich an, um Favoriten zu speichern.</p>

        @if (error()) {
          <div class="error-msg">{{ error() }}</div>
        }

        <form (ngSubmit)="onSubmit()">
          <div class="field">
            <label for="email">E-Mail</label>
            <input
              id="email"
              type="email"
              [(ngModel)]="email"
              name="email"
              autocomplete="email"
              placeholder="deine@email.ch"
            />
          </div>
          <div class="field">
            <label for="password">Passwort</label>
            <input
              id="password"
              type="password"
              [(ngModel)]="password"
              name="password"
              autocomplete="current-password"
              placeholder="••••••"
            />
          </div>
          <button type="submit" [disabled]="loading()" class="submit">
            {{ loading() ? "Wird angemeldet..." : "Anmelden" }}
          </button>
        </form>

        <p class="switch">
          Noch kein Konto?
          <a routerLink="/registrieren">Registrieren</a>
        </p>
      </div>
    </div>
  `,
  styles: `
    .page {
      display: flex;
      justify-content: center;
      padding: 4rem 1.5rem;
    }
    .card {
      width: 100%;
      max-width: 400px;
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 2rem;
    }
    h1 {
      font-size: 1.5rem;
      margin: 0 0 0.25rem;
    }
    .subtitle {
      color: var(--muted-foreground);
      font-size: 0.875rem;
      margin: 0 0 1.5rem;
    }
    .error-msg {
      background: color-mix(in srgb, #ef4444 15%, transparent);
      color: #ef4444;
      border: 1px solid color-mix(in srgb, #ef4444 30%, transparent);
      border-radius: var(--radius);
      padding: 0.625rem 0.875rem;
      font-size: 0.8125rem;
      margin-bottom: 1rem;
    }
    .field {
      margin-bottom: 1rem;
    }
    label {
      display: block;
      font-size: 0.8125rem;
      font-weight: 500;
      margin-bottom: 0.375rem;
      color: var(--foreground);
    }
    input {
      width: 100%;
      background: var(--background);
      color: var(--foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.625rem 0.75rem;
      font-size: 0.875rem;
      outline: none;
      transition: border-color 0.15s;
      box-sizing: border-box;
    }
    input:focus {
      border-color: var(--primary);
    }
    input::placeholder {
      color: var(--muted-foreground);
    }
    .submit {
      width: 100%;
      background: var(--primary);
      color: var(--primary-foreground);
      border: none;
      border-radius: var(--radius);
      padding: 0.625rem;
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      transition: opacity 0.15s;
      margin-top: 0.5rem;
    }
    .submit:hover:not(:disabled) {
      opacity: 0.9;
    }
    .submit:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .switch {
      text-align: center;
      font-size: 0.8125rem;
      color: var(--muted-foreground);
      margin: 1.25rem 0 0;
    }
    .switch a {
      color: var(--primary);
      text-decoration: none;
    }
    .switch a:hover {
      text-decoration: underline;
    }
  `,
})
export class Login {
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  email = "";
  password = "";
  error = signal<string | null>(null);
  loading = signal(false);

  onSubmit(): void {
    this.error.set(null);

    const emailVal = this.email.trim();
    const passwordVal = this.password;

    if (!emailVal || !passwordVal) {
      this.error.set("Bitte fülle alle Felder aus.");
      return;
    }

    this.loading.set(true);

    const request: LoginRequest = { email: emailVal, password: passwordVal };
    const returnUrl = this.route.snapshot.queryParams["returnUrl"] || "/";

    this.authService.login(request).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate([returnUrl]);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 401) {
          this.error.set("Ungültige E-Mail oder Passwort.");
        } else {
          this.error.set("Anmeldung fehlgeschlagen. Bitte versuche es erneut.");
        }
      },
    });
  }
}
