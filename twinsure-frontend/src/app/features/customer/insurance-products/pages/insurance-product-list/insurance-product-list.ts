// Displays active insurance products available to the Customer.
// Signals manage page-local loading, data, and error state.

import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { InsuranceProduct } from '../../models/insurance-product.model';
import { InsuranceProductApiService } from '../../services/insurance-product-api.service';

@Component({
  selector: 'app-insurance-product-list',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './insurance-product-list.html',
  styleUrl: './insurance-product-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InsuranceProductList implements OnInit {
  private readonly productApi = inject(InsuranceProductApiService);

  readonly products = signal<InsuranceProduct[]>([]);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  readonly loadingItems = [1, 2, 3];

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.error.set(null);

    this.productApi
      .getActiveProducts()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (products) => this.products.set(products),

        error: (error) => this.error.set(getApiErrorMessage(error)),
      });
  }
}
