import { Routes } from "@angular/router";
import { Home } from "./features/home/home";
import { EventDetailPage } from "./features/event-detail/event-detail";
import { NotFound } from "./features/not-found/not-found";

export const routes: Routes = [
  { path: "", component: Home },
  { path: "events/:id", component: EventDetailPage },
  { path: "**", component: NotFound },
];
