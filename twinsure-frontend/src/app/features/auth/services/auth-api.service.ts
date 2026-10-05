// Provides typed authentication requests through TwinSure API Gateway.
// Credential forwarding is handled centrally by the HTTP interceptor.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { LoginRequest } from '../models/login-request.dto';
import { LoginResponse } from '../models/login-response.dto';
import { RegisterRequest } from '../models/register-request.dto';
import { RegistrationResponse } from '../models/registration-response.dto';

@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly http = inject(HttpClient);

  private readonly authUrl = `${environment.apiBaseUrl}/api/auth`;

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/login`, request);
  }

  register(request: RegisterRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.authUrl}/register`, request);
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.authUrl}/logout`, {});
  }
}
