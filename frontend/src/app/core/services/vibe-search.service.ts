/**
 * Service für die semantische Vibe-Suche.
 *
 * Sendet Suchanfragen an den Vibe-Search-Endpunkt des Backends.
 */
import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import {
  VibeSearchRequest,
  VibeSearchResponse,
} from "../models/vibe-search.model";

@Injectable({ providedIn: "root" })
export class VibeSearchService {
  /** HTTP-Client für API-Aufrufe. */
  private readonly http = inject(HttpClient);
  /** URL des Vibe-Search-Endpunkts. */
  private readonly apiUrl = `${environment.apiUrl}/search/vibe`;

  /**
   * Führt eine Vibe-Suche durch.
   *
   * @param request die Suchanfrage mit Query und optionalen Filtern
   * @returns Observable mit den Suchergebnissen
   */
  search(request: VibeSearchRequest): Observable<VibeSearchResponse> {
    return this.http.post<VibeSearchResponse>(this.apiUrl, request);
  }
}
