/**
 * Zentrale Angular-Anwendungskonfiguration.
 *
 * Registriert Router, HTTP-Client mit Auth-Interceptor und
 * initialisiert den FavoriteService beim App-Start.
 */
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from "@angular/core";
import { provideRouter } from "@angular/router";
import {
  provideHttpClient,
  withInterceptors,
  withFetch,
} from "@angular/common/http";

import { routes } from "./app.routes";
import { authInterceptor } from "./core/interceptors/auth.interceptor";
import { FavoriteService } from "./core/services/favorite.service";

/** Anwendungskonfiguration mit allen Providern. */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor]), withFetch()),
    provideAppInitializer(() => {
      inject(FavoriteService).init();
    }),
  ],
};
