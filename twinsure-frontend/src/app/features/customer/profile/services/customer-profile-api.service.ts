// Provides typed Customer profile requests through the API Gateway.
// The credentials interceptor automatically sends the HttpOnly JWT cookie.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { CustomerProfileRequest } from '../models/customer-profile-request.model';
import { CustomerProfileResponse } from '../models/customer-profile-response.model';

@Injectable({
  providedIn: 'root',
})
export class CustomerProfileApiService {
  private readonly http = inject(HttpClient);

  private readonly profileUrl = `${environment.apiBaseUrl}/api/customers/profile`;

  getProfile(): Observable<CustomerProfileResponse> {
    return this.http.get<CustomerProfileResponse>(this.profileUrl);
  }

  createProfile(request: CustomerProfileRequest): Observable<CustomerProfileResponse> {
    return this.http.post<CustomerProfileResponse>(this.profileUrl, request);
  }

  updateProfile(request: CustomerProfileRequest): Observable<CustomerProfileResponse> {
    return this.http.put<CustomerProfileResponse>(this.profileUrl, request);
  }
}
