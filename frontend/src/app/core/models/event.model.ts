export interface Event {
  id: string;
  name: string;
  imageUrl: string;
  startDate: string;
  categoryName: string;
  locationName: string;
  city: string;
}

export interface EventDetail {
  id: string;
  name: string;
  description: string | null;
  imageUrl: string | null;
  ticketUrl: string | null;
  startDate: string;
  endDate: string | null;
  categoryName: string | null;
  categorySlug: string | null;
  locationName: string | null;
  city: string | null;
  country: string | null;
  latitude: number | null;
  longitude: number | null;
  favorited: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface EventFilter {
  page?: number;
  size?: number;
  categoryId?: number | null;
  city?: string | null;
  dateFrom?: string | null;
  dateTo?: string | null;
  search?: string | null;
}
