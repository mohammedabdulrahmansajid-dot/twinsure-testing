// Provides Administrator Customer oversight through the API Gateway.
// Admin can retrieve all Customers and update profile status.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import {
  AdminCustomer,
  UpdateCustomerStatusRequest,
} from '../models/admin-customer.model';

@Injectable({
  providedIn: 'root',
})
export class AdminCustomerApiService {
  private readonly http = inject(HttpClient);

  private readonly customersUrl =
    `${environment.apiBaseUrl}/api/admin/customers`;

  getAllCustomers(): Observable<AdminCustomer[]> {
    return this.http.get<AdminCustomer[]>(
      this.customersUrl,
    );
  }

  updateCustomerStatus(
    customerId: number,
    request: UpdateCustomerStatusRequest,
  ): Observable<AdminCustomer> {
    return this.http.put<AdminCustomer>(
      `${this.customersUrl}/${customerId}/status`,
      request,
    );
  }
}