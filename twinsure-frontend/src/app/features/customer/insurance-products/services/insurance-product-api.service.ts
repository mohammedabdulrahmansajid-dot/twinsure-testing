// Loads active insurance products through TwinSure API Gateway.
// Credential forwarding remains centralized in the HTTP interceptor.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { InsuranceProduct } from '../models/insurance-product.model';

import { InsuranceProductDetails } from '../models/insurance-product-details.model';
@Injectable({
  providedIn: 'root',
})
export class InsuranceProductApiService {
  private readonly http = inject(HttpClient);

  private readonly productsUrl = `${environment.apiBaseUrl}/api/insurance-products`;

  getActiveProducts(): Observable<InsuranceProduct[]> {
    return this.http.get<InsuranceProduct[]>(this.productsUrl);
  }

  getProductDetails(
  productId: number
): Observable<InsuranceProductDetails> {

  return this.http.get<InsuranceProductDetails>(
    `${this.productsUrl}/${productId}`
  );
}


}
