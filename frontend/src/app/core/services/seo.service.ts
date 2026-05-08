/**
 * Service für die Verwaltung von SEO-Meta-Tags.
 *
 * Setzt den Seitentitel und die Meta-Description
 * für jede Seite der Anwendung.
 */
import { Injectable, inject } from "@angular/core";
import { Meta, Title } from "@angular/platform-browser";

@Injectable({ providedIn: "root" })
export class SeoService {
  /** Angular Title-Service für den Seitentitel. */
  private readonly title = inject(Title);
  /** Angular Meta-Service für Meta-Tags. */
  private readonly meta = inject(Meta);

  /** Markenname für den Seitentitel-Suffix. */
  private readonly brand = "Löte";

  /**
   * Setzt den Seitentitel und die Meta-Description.
   *
   * @param pageTitle der Seitentitel (wird mit Markenname ergänzt)
   * @param description die Meta-Description
   */
  set(pageTitle: string, description: string): void {
    this.title.setTitle(`${pageTitle} – ${this.brand}`);
    this.meta.updateTag({ name: "description", content: description });
  }
}
