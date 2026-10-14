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
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/events`;

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

  getEvent(id: string): Observable<EventDetail> {
    return this.http.get<EventDetail>(`${this.apiUrl}/${id}`);
  }
}
