// Displays every insurance product for Administrator management.
// Admin can search products, inspect financial terms, and update
// catalogue status before opening the complete product configuration.

import { CurrencyPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import {
  AdminInsuranceProduct,
  AdminProductStatus,
} from '../../models/admin-insurance-product.model';
import { AdminInsuranceProductApiService } from '../../services/admin-insurance-product-api.service';

type ProductStatusFilter =
  | 'ALL'
  | AdminProductStatus;

@Component({
  selector: 'app-admin-insurance-product-list',
  imports: [
    CurrencyPipe,
    FormsModule,
    RouterLink,
  ],
  templateUrl: './admin-insurance-product-list.html',
  styleUrl: './admin-insurance-product-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminInsuranceProductList implements OnInit {
  private readonly productApi =
    inject(AdminInsuranceProductApiService);

  readonly products =
    signal<AdminInsuranceProduct[]>([]);

  readonly loading =
    signal(true);

  readonly error =
    signal<string | null>(null);

  readonly success =
    signal<string | null>(null);

  readonly updatingProductId =
    signal<number | null>(null);

  readonly searchText =
    signal('');

  readonly statusFilter =
    signal<ProductStatusFilter>('ALL');

  readonly statusSelections =
    signal<Record<number, AdminProductStatus>>({});

  readonly statusOptions: ProductStatusFilter[] = [
    'ALL',
    'DRAFT',
    'ACTIVE',
    'INACTIVE',
  ];

  readonly filteredProducts = computed(() => {
    const search =
      this.searchText()
        .trim()
        .toLowerCase();

    const selectedStatus =
      this.statusFilter();

    return [...this.products()]
      .filter((product) => {
        const searchMatches =
          search.length === 0
          || product.productCode
            .toLowerCase()
            .includes(search)
          || product.productName
            .toLowerCase()
            .includes(search)
          || product.description
            .toLowerCase()
            .includes(search)
          || product.productId
            .toString()
            .includes(search);

        const statusMatches =
          selectedStatus === 'ALL'
          || product.status === selectedStatus;

        return searchMatches
          && statusMatches;
      })
      .sort(
        (first, second) =>
          first.productName.localeCompare(
            second.productName,
          ),
      );
  });

  readonly activeCount = computed(() =>
    this.products().filter(
      (product) =>
        product.status === 'ACTIVE',
    ).length,
  );

  readonly draftCount = computed(() =>
    this.products().filter(
      (product) =>
        product.status === 'DRAFT',
    ).length,
  );

  readonly inactiveCount = computed(() =>
    this.products().filter(
      (product) =>
        product.status === 'INACTIVE',
    ).length,
  );

  ngOnInit(): void {
    this.loadProducts();
  }

  reload(): void {
    this.loadProducts();
  }

  updateSearch(
    value: string,
  ): void {
    this.searchText.set(value);
  }

  updateStatusFilter(
    value: string,
  ): void {
    this.statusFilter.set(
      value as ProductStatusFilter,
    );
  }

updateStatusSelection(
  productId: number,
  value: string,
): void {
  const selectedStatus =
    value as AdminProductStatus;

  this.statusSelections.update(
    (currentSelections) => ({
      ...currentSelections,
       [productId]: selectedStatus, 
    }),
  );
}

  selectedStatus(
    product: AdminInsuranceProduct,
  ): AdminProductStatus {
    return this.statusSelections()[
      product.productId
    ] ?? product.status;
  }

  saveStatus(
    product: AdminInsuranceProduct,
  ): void {
    this.error.set(null);
    this.success.set(null);

    const selectedStatus =
      this.selectedStatus(product);

    if (selectedStatus === product.status) {
      this.error.set(
        'Select a different product status.',
      );

      return;
    }

    this.updatingProductId.set(
      product.productId,
    );

    this.productApi
      .updateProductStatus(
        product.productId,
        {
          status: selectedStatus,
        },
      )
      .pipe(
        finalize(() =>
          this.updatingProductId.set(null),
        ),
      )
      .subscribe({
        next: (updatedProduct) => {
          this.products.update(
            (currentProducts) =>
              currentProducts.map(
                (existingProduct) =>
                  existingProduct.productId
                    === updatedProduct.productId
                    ? updatedProduct
                    : existingProduct,
              ),
          );

          this.statusSelections.update(
            (currentSelections) => ({
              ...currentSelections,
              [updatedProduct.productId]:
                updatedProduct.status,
            }),
          );

          this.success.set(
            `${updatedProduct.productName} is now ${this.formatLabel(
              updatedProduct.status,
            )}.`,
          );
        },

        error: (error: unknown) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }

  clearFilters(): void {
    this.searchText.set('');
    this.statusFilter.set('ALL');
  }

  formatLabel(
    value: string,
  ): string {
    return value
      .toLowerCase()
      .split('_')
      .map(
        (part) =>
          part.charAt(0).toUpperCase()
          + part.slice(1),
      )
      .join(' ');
  }

  statusClasses(
    status: AdminProductStatus,
  ): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-100 text-emerald-700';

      case 'DRAFT':
        return 'bg-amber-100 text-amber-800';

      case 'INACTIVE':
        return 'bg-slate-200 text-slate-700';

      default:
        return 'bg-blue-100 text-blue-700';
    }
  }

  private loadProducts(): void {
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);

    this.productApi
      .getAllProducts()
      .pipe(
        finalize(() =>
          this.loading.set(false),
        ),
      )
      .subscribe({
        next: (products) => {
          this.products.set(products);

          const selections:
            Record<number, AdminProductStatus> = {};

          for (const product of products) {
            selections[product.productId] =
              product.status;
          }

          this.statusSelections.set(
            selections,
          );
        },

        error: (error: unknown) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }
}