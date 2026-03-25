import { Injectable, inject } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { Event, PagedResponse } from "../models/event.model";

@Injectable({ providedIn: "root" })
export class EventService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/events`;

  getEvents(
    params: { page?: number; size?: number } = {},
  ): Observable<PagedResponse<Event>> {
    let httpParams = new HttpParams();

    if (params.page != null) {
      httpParams = httpParams.set("page", params.page.toString());
    }
    if (params.size != null) {
      httpParams = httpParams.set("size", params.size.toString());
    }

    return this.http.get<PagedResponse<Event>>(this.apiUrl, {
      params: httpParams,
    });
  }
}
