import { Event } from "./event.model";

export interface VibeSearchRequest {
  query: string;
  categoryId?: number | null;
  city?: string | null;
  dateFrom?: string | null;
  dateTo?: string | null;
  limit?: number | null;
}

export interface VibeSearchResponse {
  results: Event[];
  fallback: boolean;
}
