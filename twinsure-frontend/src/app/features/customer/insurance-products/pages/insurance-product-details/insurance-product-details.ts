// Displays complete product coverage and exclusion information.
// Signals manage this page-local read-only API state.

import {
  CurrencyPipe
} from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal
} from '@angular/core';
import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';
import {
  finalize
} from 'rxjs';

import {
  getApiErrorMessage
} from '../../../../../core/http/api-error.util';
import {
  InsuranceProductDetails as ProductDetails
} from '../../models/insurance-product-details.model';
import {
  InsuranceProductApiService
} from '../../services/insurance-product-api.service';

@Component({
  selector: 'app-insurance-product-details',
  imports: [
    CurrencyPipe,
    RouterLink
  ],
  templateUrl: './insurance-product-details.html',
  styleUrl: './insurance-product-details.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InsuranceProductDetails
  implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly productApi =
    inject(InsuranceProductApiService);

  private readonly productId =
    Number(
      this.route.snapshot.paramMap.get(
        'productId'
      )
    );

  readonly product =
    signal<ProductDetails | null>(
      null
    );

  readonly loading =
    signal(true);

  readonly error =
    signal<string | null>(
      null
    );

  readonly activeCoverages =
    computed(() =>
      this.product()?.coverages.filter(
        coverage => coverage.active
      ) ?? []
    );

  readonly activeExclusions =
    computed(() =>
      this.product()?.exclusions.filter(
        exclusion => exclusion.active
      ) ?? []
    );

  ngOnInit(): void {

    if (
      !Number.isInteger(this.productId)
      || this.productId <= 0
    ) {
      this.router.navigateByUrl(
        '/customer/insurance-products'
      );

      return;
    }

    this.loadProduct();
  }

  formatLabel(
    value: string
  ): string {

    return value
      .toLowerCase()
      .split('_')
      .map(part =>
        part.charAt(0).toUpperCase()
          + part.slice(1)
      )
      .join(' ');
  }

  private loadProduct(): void {

    this.loading.set(true);
    this.error.set(null);

    this.productApi
      .getProductDetails(
        this.productId
      )
      .pipe(
        finalize(() =>
          this.loading.set(false)
        )
      )
      .subscribe({
        next: product =>
          this.product.set(product),

        error: error =>
          this.error.set(
            getApiErrorMessage(error)
          )
      });
  }
}