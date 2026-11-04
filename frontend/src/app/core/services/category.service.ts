/**
 * Service für den Zugriff auf Kategorie-Daten.
 *
 * Ruft die Event-Kategorien vom Backend ab.
 */
import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { Category } from "../models/category.model";

@Injectable({ providedIn: "root" })
export class CategoryService {
  /** HTTP-Client für API-Aufrufe. */
  private readonly http = inject(HttpClient);

  /**
   * Ruft alle Event-Kategorien ab.
   *
   * @returns Observable mit der Kategorie-Liste
   */
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${environment.apiUrl}/categories`);
  }
}
