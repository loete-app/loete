export interface SampleItem {
  id: string;
  title: string;
  description: string;
  category: ItemCategory;
  priority: Priority;
  price: number;
  createdAt: string;
}

export type ItemCategory =
  | "BOOK"
  | "ELECTRONICS"
  | "CLOTHING"
  | "FOOD"
  | "OTHER";

export type Priority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";

export interface CreateItemRequest {
  title: string;
  description?: string;
  category: ItemCategory;
  priority: Priority;
  price: number;
}
