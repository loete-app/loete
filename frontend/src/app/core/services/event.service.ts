/**
 * Service für den Zugriff auf Event-Daten.
 *
 * Stellt Methoden für die paginierte Event-Liste und die
 * Event-Detailansicht bereit.
 */
import { Injectable, inject } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import {
  Event,
  EventDetail,
  EventFilter,
  PagedResponse,
} from "../models/event.model";

@Injectable({ providedIn: "root" })
export class EventService {
  /** HTTP-Client für API-Aufrufe. */
  private readonly http = inject(HttpClient);
  /** Basis-URL der Event-API-Endpunkte. */
  private readonly apiUrl = `${environment.apiUrl}/events`;

  /**
   * Ruft eine paginierte, gefilterte Liste von Events ab.
   *
   * @param filter die Filterkriterien und Paginierungsparameter
   * @returns Observable mit der paginierten Event-Liste
   */
  getEvents(filter: EventFilter = {}): Observable<PagedResponse<Event>> {
    let params = new HttpParams();
    if (filter.page != null)
      params = params.set("page", filter.page.toString());
    if (filter.size != null)
      params = params.set("size", filter.size.toString());
    if (filter.categoryId != null)
      params = params.set("categoryId", filter.categoryId.toString());
    if (filter.city) params = params.set("city", filter.city);
    if (filter.dateFrom) params = params.set("dateFrom", filter.dateFrom);
    if (filter.dateTo) params = params.set("dateTo", filter.dateTo);
    if (filter.search) params = params.set("search", filter.search);

    return this.http.get<PagedResponse<Event>>(this.apiUrl, { params });
  }

  /**
   * Ruft die Detailansicht eines Events ab.
   *
   * @param id die Event-ID
   * @returns Observable mit den Event-Details
   */
  getEvent(id: string): Observable<EventDetail> {
    return this.http.get<EventDetail>(`${this.apiUrl}/${id}`);
  }
}
