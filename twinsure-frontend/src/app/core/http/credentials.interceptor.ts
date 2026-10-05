// Adds browser credentials only to requests sent through TwinSure Gateway.
// This allows the browser to send the TWINSURE_TOKEN HttpOnly cookie
// without exposing the token to Angular code.

import { HttpInterceptorFn } from '@angular/common/http';

import { environment } from '../../../environments/environment';

export const credentialsInterceptor: HttpInterceptorFn =
  (request, next) => {

    const isGatewayRequest =
      request.url.startsWith(environment.apiBaseUrl);

    if (!isGatewayRequest) {
      return next(request);
    }

    const credentialedRequest = request.clone({
      withCredentials: true
    });

    return next(credentialedRequest);
  };