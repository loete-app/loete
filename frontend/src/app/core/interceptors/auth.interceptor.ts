/**
 * Funktionaler HTTP-Interceptor für die JWT-Authentifizierung.
 *
 * Fügt das Bearer-Token an ausgehende Requests an (ausser Auth-Endpunkte).
 * Bei 401-Antworten wird versucht, das Token über den Refresh-Endpunkt
 * zu erneuern und den Request erneut auszufuehren.
 */
import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { catchError, switchMap, throwError } from "rxjs";
import { AuthService } from "../services/auth.service";

/** Interceptor-Funktion für die automatische Token-Verwaltung. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  if (req.url.includes("/auth/")) {
    return next(req);
  }

  const token = authService.getToken();
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && authService.getRefreshToken()) {
        return authService.refreshAccessToken().pipe(
          switchMap((res) => {
            const retryReq = req.clone({
              setHeaders: { Authorization: `Bearer ${res.accessToken}` },
            });
            return next(retryReq);
          }),
          catchError(() => {
            authService.logout();
            return throwError(() => error);
          }),
        );
      }
      return throwError(() => error);
    }),
  );
};
