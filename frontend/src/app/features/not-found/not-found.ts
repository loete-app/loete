/**
 * 404-Fehlerseite.
 *
 * Wird angezeigt, wenn eine nicht existierende Route aufgerufen wird.
 * Bietet einen Link zurück zur Startseite.
 */
import { Component, inject, OnInit } from "@angular/core";
import { RouterLink } from "@angular/router";
import { SeoService } from "@/core/services/seo.service";

@Component({
  selector: "app-not-found",
  imports: [RouterLink],
  template: `
    <section class="page">
      <h1>404</h1>
      <p>Page not found.</p>
      <a routerLink="/">Back to home</a>
    </section>
  `,
  styles: `
    .page {
      max-width: 720px;
      margin: 0 auto;
      padding: 4rem 1.5rem;
      text-align: center;
    }
    h1 {
      font-size: 4rem;
      color: var(--primary);
    }
    p {
      color: var(--muted-foreground);
      margin-bottom: 1.5rem;
    }
    a {
      color: var(--primary);
    }
  `,
})
export class NotFound implements OnInit {
  /** Service für SEO-Meta-Tags. */
  private seo = inject(SeoService);

  /** Setzt die SEO-Meta-Tags für die 404-Seite. */
  ngOnInit(): void {
    this.seo.set("Seite nicht gefunden", "Diese Seite existiert nicht.");
  }
}
