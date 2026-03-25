export interface Event {
  id: string;
  name: string;
  imageUrl: string;
  startDate: string;
  categoryName: string;
  locationName: string;
  city: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
