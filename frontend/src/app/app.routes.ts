import { Routes } from "@angular/router";
import { Home } from "./features/home/home";
import { ItemDetail } from "./features/item-detail/item-detail";
import { NotFound } from "./features/not-found/not-found";

export const routes: Routes = [
  { path: "", component: Home },
  { path: "items/:id", component: ItemDetail },
  { path: "**", component: NotFound },
];
