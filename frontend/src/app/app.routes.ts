import { Routes } from "@angular/router";
import { Home } from "./features/home/home";
import { EventDetailPage } from "./features/event-detail/event-detail";
import { Favorites } from "./features/favorites/favorites";
import { Login } from "./features/auth/login/login";
import { Register } from "./features/auth/register/register";
import { NotFound } from "./features/not-found/not-found";
export const routes: Routes = [
  { path: "", component: Home },
  { path: "login", component: Login },
  { path: "registrieren", component: Register },
  { path: "events/:id", component: EventDetailPage },
  { path: "favoriten", component: Favorites },
  { path: "**", component: NotFound },
];
