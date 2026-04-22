export interface Favorite {
  id: string;
  eventId: string;
  name: string;
  imageUrl: string | null;
  startDate: string;
  categoryName: string | null;
  locationName: string | null;
  city: string | null;
  createdAt: string;
}
