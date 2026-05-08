/**
 * Funktionaler Route-Guard für authentifizierte Bereiche.
 *
 * Prüft, ob der Benutzer eingeloggt ist. Bei fehlender Authentifizierung
 * wird auf die Login-Seite umgeleitet (mit returnUrl als Query-Parameter).
 */
import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../services/auth.service";

/** Guard-Funktion, die den Zugang zu geschützten Routen kontrolliert. */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }
  return router.createUrlTree(["/login"], {
    queryParams: { returnUrl: state.url },
  });
};
