import { Injectable, inject } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import {
  CreateItemRequest,
  ItemCategory,
  Priority,
  SampleItem,
} from "../models/item.model";

@Injectable({ providedIn: "root" })
export class ItemService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/sample/items`;

  getItems(filters?: {
    category?: ItemCategory;
    priority?: Priority;
  }): Observable<SampleItem[]> {
    let params = new HttpParams();
    if (filters?.category) params = params.set("category", filters.category);
    if (filters?.priority) params = params.set("priority", filters.priority);
    return this.http.get<SampleItem[]>(this.apiUrl, { params });
  }

  getItem(id: string): Observable<SampleItem> {
    return this.http.get<SampleItem>(`${this.apiUrl}/${id}`);
  }

  createItem(request: CreateItemRequest): Observable<SampleItem> {
    return this.http.post<SampleItem>(this.apiUrl, request);
  }

  deleteItem(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
