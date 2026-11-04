/**
 * Service für den Zugriff auf Location-Daten.
 *
 * Ruft die verfügbaren Städte mit Events vom Backend ab.
 */
import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class LocationService {
  /** HTTP-Client für API-Aufrufe. */
  private readonly http = inject(HttpClient);

  /**
   * Ruft alle Städte ab, in denen Events stattfinden.
   *
   * @returns Observable mit der alphabetisch sortierten Städte-Liste
   */
  getCities(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}/locations/cities`);
  }
}
