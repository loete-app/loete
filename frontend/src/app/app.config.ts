import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
} from "@angular/core";
import { provideRouter } from "@angular/router";
import {
  provideHttpClient,
  withInterceptors,
  withFetch,
} from "@angular/common/http";
import {
  provideClientHydration,
  withEventReplay,
} from "@angular/platform-browser";

import { routes } from "./app.routes";
import { authInterceptor } from "./core/interceptors/auth.interceptor";
import { clientIdInterceptor } from "./core/interceptors/client-id.interceptor";

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([clientIdInterceptor, authInterceptor]),
      withFetch(),
    ),
    provideClientHydration(withEventReplay()),
  ],
};
