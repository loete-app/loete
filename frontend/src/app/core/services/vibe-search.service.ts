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
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/search/vibe`;

  search(request: VibeSearchRequest): Observable<VibeSearchResponse> {
    return this.http.post<VibeSearchResponse>(this.apiUrl, request);
  }
}
