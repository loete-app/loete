/**
 * Seite zum Erstellen eines eigenen Events.
 *
 * Zeigt ein Formular mit allgemeinen Angaben, Datum, Veranstaltungsort
 * sowie optionalem Bild- und Ticket-Link an. Validiert die Eingaben
 * clientseitig und leitet nach erfolgreicher Erstellung auf die
 * Detailseite des neuen Events weiter. Nur für eingeloggte Benutzer.
 */
import { Component, inject, OnInit, signal } from "@angular/core";
import { Location } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import {
  LucideAngularModule,
  ArrowLeft,
  CalendarPlus,
  Calendar,
  MapPin,
  Link,
  Info,
} from "lucide-angular";
import { EventService } from "@/core/services/event.service";
import { CategoryService } from "@/core/services/category.service";
import { LocationService } from "@/core/services/location.service";
import { SeoService } from "@/core/services/seo.service";
import { Category } from "@/core/models/category.model";
import { CreateEventRequest } from "@/core/models/event.model";

/** Mögliche Formularfelder mit eigener Fehlermeldung. */
type Field =
  | "name"
  | "categoryId"
  | "description"
  | "startDate"
  | "endDate"
  | "locationName"
  | "city"
  | "imageUrl"
  | "ticketUrl";

/** Maximale Länge der Beschreibung (entspricht der Backend-Validierung). */
const DESCRIPTION_MAX = 5000;

@Component({
  selector: "app-create-event",
  imports: [FormsModule, LucideAngularModule],
  template: `
    <div class="page">
      <button class="back" type="button" (click)="goBack()">
        <i-lucide [img]="BackIcon" [size]="16" />
        Zurück
      </button>

      <header class="head">
        <h1>Event erstellen</h1>
        <p class="subtitle">
          Teile dein eigenes Event mit allen auf Löte. Felder mit * sind
          Pflichtfelder.
        </p>
      </header>

      @if (error()) {
        <div class="error-msg" role="alert">{{ error() }}</div>
      }

      <form (ngSubmit)="onSubmit()" novalidate>
        <section class="card">
          <h2>
            <i-lucide [img]="InfoIcon" [size]="16" />
            Allgemein
          </h2>
          <div class="field">
            <label for="name">Titel *</label>
            <input
              id="name"
              type="text"
              [(ngModel)]="name"
              name="name"
              maxlength="255"
              placeholder="z.B. Sommerfest am See"
              [class.invalid]="fieldErrors()['name']"
            />
            @if (fieldErrors()["name"]; as msg) {
              <p class="field-error">{{ msg }}</p>
            }
          </div>
          <div class="field">
            <label for="category">Kategorie *</label>
            <select
              id="category"
              [(ngModel)]="categoryId"
              name="categoryId"
              [class.invalid]="fieldErrors()['categoryId']"
            >
              <option [ngValue]="null" disabled>Kategorie wählen</option>
              @for (cat of categories(); track cat.id) {
                <option [ngValue]="cat.id">{{ cat.name }}</option>
              }
            </select>
            @if (fieldErrors()["categoryId"]; as msg) {
              <p class="field-error">{{ msg }}</p>
            }
          </div>
          <div class="field">
            <label for="description">Beschreibung</label>
            <textarea
              id="description"
              [(ngModel)]="description"
              name="description"
              rows="5"
              [attr.maxlength]="descriptionMax"
              placeholder="Worum geht es? Was erwartet die Besucher?"
              [class.invalid]="fieldErrors()['description']"
            ></textarea>
            <p class="hint">
              {{ description.length }} / {{ descriptionMax }} Zeichen
            </p>
            @if (fieldErrors()["description"]; as msg) {
              <p class="field-error">{{ msg }}</p>
            }
          </div>
        </section>

        <section class="card">
          <h2>
            <i-lucide [img]="CalendarIcon" [size]="16" />
            Datum &amp; Zeit
          </h2>
          <div class="grid">
            <div class="field">
              <label for="startDate">Beginn *</label>
              <input
                id="startDate"
                type="datetime-local"
                [(ngModel)]="startDate"
                name="startDate"
                [min]="minDate"
                [class.invalid]="fieldErrors()['startDate']"
              />
              @if (fieldErrors()["startDate"]; as msg) {
                <p class="field-error">{{ msg }}</p>
              }
            </div>
            <div class="field">
              <label for="endDate">Ende</label>
              <input
                id="endDate"
                type="datetime-local"
                [(ngModel)]="endDate"
                name="endDate"
                [min]="startDate || minDate"
                [class.invalid]="fieldErrors()['endDate']"
              />
              @if (fieldErrors()["endDate"]; as msg) {
                <p class="field-error">{{ msg }}</p>
              }
            </div>
          </div>
        </section>

        <section class="card">
          <h2>
            <i-lucide [img]="MapPinIcon" [size]="16" />
            Ort
          </h2>
          <div class="grid">
            <div class="field">
              <label for="locationName">Veranstaltungsort *</label>
              <input
                id="locationName"
                type="text"
                [(ngModel)]="locationName"
                name="locationName"
                maxlength="255"
                placeholder="z.B. Landiwiese"
                [class.invalid]="fieldErrors()['locationName']"
              />
              @if (fieldErrors()["locationName"]; as msg) {
                <p class="field-error">{{ msg }}</p>
              }
            </div>
            <div class="field">
              <label for="city">Stadt *</label>
              <input
                id="city"
                type="text"
                [(ngModel)]="city"
                name="city"
                maxlength="255"
                list="city-options"
                autocomplete="address-level2"
                placeholder="z.B. Zürich"
                [class.invalid]="fieldErrors()['city']"
              />
              <datalist id="city-options">
                @for (c of cities(); track c) {
                  <option [value]="c"></option>
                }
              </datalist>
              @if (fieldErrors()["city"]; as msg) {
                <p class="field-error">{{ msg }}</p>
              }
            </div>
          </div>
        </section>

        <section class="card">
          <h2>
            <i-lucide [img]="LinkIcon" [size]="16" />
            Bild &amp; Links
          </h2>
          <div class="field">
            <label for="imageUrl">Bild-URL</label>
            <input
              id="imageUrl"
              type="url"
              [(ngModel)]="imageUrl"
              (ngModelChange)="previewFailed.set(false)"
              name="imageUrl"
              maxlength="1024"
              placeholder="https://..."
              [class.invalid]="fieldErrors()['imageUrl']"
            />
            @if (fieldErrors()["imageUrl"]; as msg) {
              <p class="field-error">{{ msg }}</p>
            }
            @if (isHttpUrl(imageUrl) && !previewFailed()) {
              <div class="preview">
                <img
                  [src]="imageUrl.trim()"
                  alt="Vorschau des Event-Bildes"
                  (error)="previewFailed.set(true)"
                />
              </div>
            } @else if (previewFailed()) {
              <p class="hint">Das Bild konnte nicht geladen werden.</p>
            }
          </div>
          <div class="field">
            <label for="ticketUrl">Ticket- oder Info-Link</label>
            <input
              id="ticketUrl"
              type="url"
              [(ngModel)]="ticketUrl"
              name="ticketUrl"
              maxlength="1024"
              placeholder="https://..."
              [class.invalid]="fieldErrors()['ticketUrl']"
            />
            @if (fieldErrors()["ticketUrl"]; as msg) {
              <p class="field-error">{{ msg }}</p>
            }
          </div>
        </section>

        <div class="actions">
          <button type="button" class="cancel" (click)="goBack()">
            Abbrechen
          </button>
          <button type="submit" class="submit" [disabled]="loading()">
            <i-lucide [img]="CreateIcon" [size]="16" />
            {{ loading() ? "Wird erstellt..." : "Event erstellen" }}
          </button>
        </div>
      </form>
    </div>
  `,
  styles: `
    .page {
      max-width: 720px;
      margin: 0 auto;
      padding: 1.5rem;
    }
    .back {
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
      background: transparent;
      color: var(--muted-foreground);
      border: none;
      padding: 0.5rem 0;
      margin-bottom: 1rem;
      font-size: 0.875rem;
      cursor: pointer;
    }
    .back:hover {
      color: var(--foreground);
    }
    .head {
      margin-bottom: 1.5rem;
    }
    .head h1 {
      font-size: 1.75rem;
      margin: 0 0 0.25rem;
    }
    .subtitle {
      color: var(--muted-foreground);
      font-size: 0.9375rem;
      margin: 0;
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
    form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 1.5rem;
    }
    h2 {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1rem;
      margin: 0 0 1rem;
    }
    h2 i-lucide {
      color: color-mix(in srgb, var(--primary) 70%, transparent);
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 0 1rem;
    }
    @media (max-width: 600px) {
      .grid {
        grid-template-columns: 1fr;
      }
    }
    .field {
      margin-bottom: 1rem;
    }
    .field:last-child,
    .grid .field {
      margin-bottom: 0;
    }
    @media (max-width: 600px) {
      .grid .field:not(:last-child) {
        margin-bottom: 1rem;
      }
    }
    label {
      display: block;
      font-size: 0.8125rem;
      font-weight: 500;
      margin-bottom: 0.375rem;
      color: var(--foreground);
    }
    input,
    select,
    textarea {
      width: 100%;
      background: var(--background);
      color: var(--foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.625rem 0.75rem;
      font-size: 0.875rem;
      font-family: inherit;
      outline: none;
      transition: border-color 0.15s;
      box-sizing: border-box;
    }
    input[type="datetime-local"] {
      color-scheme: dark;
    }
    select {
      cursor: pointer;
    }
    textarea {
      resize: vertical;
      min-height: 7rem;
      line-height: 1.5;
    }
    input:focus,
    select:focus,
    textarea:focus {
      border-color: var(--primary);
    }
    input::placeholder,
    textarea::placeholder {
      color: var(--muted-foreground);
    }
    .invalid,
    .invalid:focus {
      border-color: #ef4444;
    }
    .hint {
      font-size: 0.75rem;
      color: var(--muted-foreground);
      margin: 0.375rem 0 0;
    }
    .field-error {
      font-size: 0.75rem;
      color: #ef4444;
      margin: 0.375rem 0 0;
    }
    .preview {
      margin-top: 0.75rem;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      overflow: hidden;
      aspect-ratio: 16 / 9;
      max-height: 240px;
    }
    .preview img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
    .actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      margin-top: 0.5rem;
    }
    .cancel {
      background: transparent;
      color: var(--muted-foreground);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 0.625rem 1rem;
      font-size: 0.875rem;
      cursor: pointer;
      transition:
        color 0.15s,
        border-color 0.15s;
    }
    .cancel:hover {
      color: var(--foreground);
      border-color: var(--foreground);
    }
    .submit {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: var(--primary);
      color: var(--primary-foreground);
      border: none;
      border-radius: var(--radius);
      padding: 0.625rem 1.25rem;
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      transition: opacity 0.15s;
    }
    .submit:hover:not(:disabled) {
      opacity: 0.9;
    }
    .submit:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `,
})
export class CreateEvent implements OnInit {
  /** Service für das Erstellen von Events. */
  private eventService = inject(EventService);
  /** Service für das Laden der Kategorien. */
  private categoryService = inject(CategoryService);
  /** Service für die Städte-Vorschläge. */
  private locationService = inject(LocationService);
  /** Service für SEO-Meta-Tags. */
  private seo = inject(SeoService);
  /** Router für die Weiterleitung nach dem Erstellen. */
  private router = inject(Router);
  /** Angular Location-Service für die Zurück-Navigation. */
  private location = inject(Location);

  /** Eingabewert für den Titel. */
  name = "";
  /** Ausgewählte Kategorie-ID. */
  categoryId: number | null = null;
  /** Eingabewert für die Beschreibung. */
  description = "";
  /** Beginn im Format des datetime-local-Feldes (yyyy-MM-ddTHH:mm). */
  startDate = "";
  /** Ende im Format des datetime-local-Feldes (optional). */
  endDate = "";
  /** Name des Veranstaltungsortes. */
  locationName = "";
  /** Stadt des Veranstaltungsortes. */
  city = "";
  /** Optionale Bild-URL. */
  imageUrl = "";
  /** Optionale Ticket- bzw. Info-URL. */
  ticketUrl = "";

  /** Verfügbare Kategorien für die Auswahl. */
  categories = signal<Category[]>([]);
  /** Bekannte Städte als Vorschläge. */
  cities = signal<string[]>([]);
  /** Feldbezogene Validierungsfehler. */
  fieldErrors = signal<Partial<Record<Field, string>>>({});
  /** Allgemeine Fehlermeldung (z.B. Serverfehler). */
  error = signal<string | null>(null);
  /** Ladezustand während des Speicherns. */
  loading = signal(false);
  /** Ob die Bildvorschau nicht geladen werden konnte. */
  previewFailed = signal(false);

  /** Maximale Länge der Beschreibung. */
  readonly descriptionMax = DESCRIPTION_MAX;
  /** Frühester wählbarer Zeitpunkt (jetzt) im datetime-local-Format. */
  readonly minDate = toLocalInputValue(new Date());

  /** Zurück-Pfeil-Icon. */
  readonly BackIcon = ArrowLeft;
  /** Icon für den Erstellen-Button. */
  readonly CreateIcon = CalendarPlus;
  /** Kalender-Icon für den Datumsabschnitt. */
  readonly CalendarIcon = Calendar;
  /** Karten-Pin-Icon für den Ortsabschnitt. */
  readonly MapPinIcon = MapPin;
  /** Link-Icon für den Abschnitt Bild & Links. */
  readonly LinkIcon = Link;
  /** Info-Icon für den allgemeinen Abschnitt. */
  readonly InfoIcon = Info;

  /** Lädt Kategorien und Städte und setzt die Meta-Tags. */
  ngOnInit(): void {
    this.seo.set("Event erstellen", "Erstelle dein eigenes Event auf Löte.");
    this.categoryService.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
      error: () =>
        this.error.set(
          "Kategorien konnten nicht geladen werden. Bitte lade die Seite neu.",
        ),
    });
    this.locationService.getCities().subscribe({
      next: (cities) => this.cities.set(cities),
      error: () => this.cities.set([]),
    });
  }

  /** Navigiert zur vorherigen Seite. */
  goBack(): void {
    this.location.back();
  }

  /**
   * Prüft, ob ein Wert eine absolute http(s)-URL ist.
   *
   * @param value der zu prüfende Wert
   * @returns true bei gültiger http(s)-URL
   */
  isHttpUrl(value: string): boolean {
    const trimmed = value.trim();
    if (!trimmed || /\s/.test(trimmed)) return false;
    try {
      const url = new URL(trimmed);
      return url.protocol === "http:" || url.protocol === "https:";
    } catch {
      return false;
    }
  }

  /** Validiert das Formular und sendet es an das Backend. */
  onSubmit(): void {
    this.error.set(null);
    const errors = this.validate();
    this.fieldErrors.set(errors);

    if (Object.keys(errors).length > 0) {
      this.error.set("Bitte überprüfe die markierten Felder.");
      return;
    }

    const request: CreateEventRequest = {
      name: this.name.trim(),
      description: this.description.trim() || null,
      imageUrl: this.imageUrl.trim() || null,
      ticketUrl: this.ticketUrl.trim() || null,
      startDate: withSeconds(this.startDate),
      endDate: this.endDate ? withSeconds(this.endDate) : null,
      categoryId: this.categoryId!,
      locationName: this.locationName.trim(),
      city: this.city.trim(),
    };

    this.loading.set(true);
    this.eventService.createEvent(request).subscribe({
      next: (created) => {
        this.loading.set(false);
        this.router.navigate(["/events", created.id]);
      },
      error: (err) => {
        this.loading.set(false);
        if (err?.status === 400) {
          this.error.set(
            "Einige Angaben sind ungültig. Bitte überprüfe deine Eingaben.",
          );
        } else if (err?.status === 404) {
          this.error.set("Die gewählte Kategorie existiert nicht mehr.");
        } else {
          this.error.set(
            "Das Event konnte nicht erstellt werden. Bitte versuche es erneut.",
          );
        }
      },
    });
  }

  /**
   * Prüft alle Eingaben und liefert die Fehlermeldungen pro Feld.
   *
   * @returns Map von Feld zu Fehlermeldung (leer, wenn alles gültig ist)
   */
  private validate(): Partial<Record<Field, string>> {
    const errors: Partial<Record<Field, string>> = {};

    if (!this.name.trim()) {
      errors.name = "Bitte gib einen Titel ein.";
    }
    if (this.categoryId == null) {
      errors.categoryId = "Bitte wähle eine Kategorie.";
    }
    if (this.description.length > DESCRIPTION_MAX) {
      errors.description = `Die Beschreibung darf höchstens ${DESCRIPTION_MAX} Zeichen lang sein.`;
    }

    const start = this.startDate ? new Date(this.startDate) : null;
    if (!start || isNaN(start.getTime())) {
      errors.startDate = "Bitte wähle Datum und Uhrzeit für den Beginn.";
    } else if (start.getTime() <= Date.now()) {
      errors.startDate = "Der Beginn muss in der Zukunft liegen.";
    }

    if (this.endDate) {
      const end = new Date(this.endDate);
      if (isNaN(end.getTime())) {
        errors.endDate = "Bitte gib ein gültiges Enddatum ein.";
      } else if (start && end.getTime() <= start.getTime()) {
        errors.endDate = "Das Ende muss nach dem Beginn liegen.";
      }
    }

    if (!this.locationName.trim()) {
      errors.locationName = "Bitte gib den Veranstaltungsort an.";
    }
    if (!this.city.trim()) {
      errors.city = "Bitte gib die Stadt an.";
    }
    if (this.imageUrl.trim() && !this.isHttpUrl(this.imageUrl)) {
      errors.imageUrl = "Bitte gib eine gültige URL ein (https://...).";
    }
    if (this.ticketUrl.trim() && !this.isHttpUrl(this.ticketUrl)) {
      errors.ticketUrl = "Bitte gib eine gültige URL ein (https://...).";
    }

    return errors;
  }
}

/**
 * Formatiert ein Datum als Wert für ein datetime-local-Feld.
 *
 * @param date das Datum
 * @returns lokaler String im Format yyyy-MM-ddTHH:mm
 */
function toLocalInputValue(date: Date): string {
  const pad = (n: number) => n.toString().padStart(2, "0");
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}`
  );
}

/**
 * Ergänzt einen datetime-local-Wert um Sekunden, falls nötig.
 *
 * @param value Wert im Format yyyy-MM-ddTHH:mm oder yyyy-MM-ddTHH:mm:ss
 * @returns Wert im Format yyyy-MM-ddTHH:mm:ss
 */
function withSeconds(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}
