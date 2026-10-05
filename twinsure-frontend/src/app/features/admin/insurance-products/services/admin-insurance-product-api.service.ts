// Provides complete insurance-product administration through API Gateway.
// Insurance Policy Service validates product terms, coverage rules,
// exclusions, uniqueness constraints, and status transitions.

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import {
  AdminInsuranceProduct,
  AdminInsuranceProductDetails,
  AdminProductCoverage,
  AdminProductExclusion,
  CreateInsuranceProductRequest,
  ProductCoverageRequest,
  ProductExclusionRequest,
  UpdateInsuranceProductRequest,
  UpdateProductStatusRequest,
} from '../models/admin-insurance-product.model';

@Injectable({
  providedIn: 'root',
})
export class AdminInsuranceProductApiService {
  private readonly http = inject(HttpClient);

  private readonly productsUrl =
    `${environment.apiBaseUrl}/api/admin/insurance-products`;

  getAllProducts(): Observable<AdminInsuranceProduct[]> {
    return this.http.get<AdminInsuranceProduct[]>(
      this.productsUrl,
    );
  }

  getProductDetails(
    productId: number,
  ): Observable<AdminInsuranceProductDetails> {
    return this.http.get<AdminInsuranceProductDetails>(
      `${this.productsUrl}/${productId}`,
    );
  }

  createProduct(
    request: CreateInsuranceProductRequest,
  ): Observable<AdminInsuranceProduct> {
    return this.http.post<AdminInsuranceProduct>(
      this.productsUrl,
      request,
    );
  }

  updateProduct(
    productId: number,
    request: UpdateInsuranceProductRequest,
  ): Observable<AdminInsuranceProduct> {
    return this.http.put<AdminInsuranceProduct>(
      `${this.productsUrl}/${productId}`,
      request,
    );
  }

  updateProductStatus(
    productId: number,
    request: UpdateProductStatusRequest,
  ): Observable<AdminInsuranceProduct> {
    return this.http.put<AdminInsuranceProduct>(
      `${this.productsUrl}/${productId}/status`,
      request,
    );
  }

  addCoverage(
    productId: number,
    request: ProductCoverageRequest,
  ): Observable<AdminProductCoverage> {
    return this.http.post<AdminProductCoverage>(
      `${this.productsUrl}/${productId}/coverages`,
      request,
    );
  }

  updateCoverage(
    productId: number,
    request: ProductCoverageRequest,
  ): Observable<AdminProductCoverage> {
    return this.http.put<AdminProductCoverage>(
      `${this.productsUrl}/${productId}/coverages`,
      request,
    );
  }

  addExclusion(
    productId: number,
    request: ProductExclusionRequest,
  ): Observable<AdminProductExclusion> {
    return this.http.post<AdminProductExclusion>(
      `${this.productsUrl}/${productId}/exclusions`,
      request,
    );
  }

  updateExclusion(
    productId: number,
    request: ProductExclusionRequest,
  ): Observable<AdminProductExclusion> {
    return this.http.put<AdminProductExclusion>(
      `${this.productsUrl}/${productId}/exclusions`,
      request,
    );
  }
}