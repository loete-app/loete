import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { ClientIdService } from "../services/client-id.service";

export const clientIdInterceptor: HttpInterceptorFn = (req, next) => {
  const clientId = inject(ClientIdService).getClientId();
  if (clientId) {
    req = req.clone({ setHeaders: { "X-Client-Id": clientId } });
  }
  return next(req);
};
