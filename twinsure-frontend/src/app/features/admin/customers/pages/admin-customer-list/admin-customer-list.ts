// Displays Customer profiles for Administrator oversight.
// Search and status filtering run locally, while Customer Service
// persists account-profile activation and suspension.

import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import {
  AdminCustomer,
  AdminCustomerStatus,
} from '../../models/admin-customer.model';
import { AdminCustomerApiService } from '../../services/admin-customer-api.service';

type CustomerStatusFilter =
  | 'ALL'
  | AdminCustomerStatus;

@Component({
  selector: 'app-admin-customer-list',
  imports: [FormsModule],
  templateUrl: './admin-customer-list.html',
  styleUrl: './admin-customer-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminCustomerList implements OnInit {
  private readonly customerApi =
    inject(AdminCustomerApiService);

  readonly customers =
    signal<AdminCustomer[]>([]);

  readonly loading = signal(true);

  readonly error =
    signal<string | null>(null);

  readonly success =
    signal<string | null>(null);

  readonly updatingCustomerId =
    signal<number | null>(null);

  readonly searchText = signal('');

  readonly statusFilter =
    signal<CustomerStatusFilter>('ALL');

  readonly statusOptions: CustomerStatusFilter[] = [
    'ALL',
    'ACTIVE',
    'SUSPENDED',
  ];

  readonly filteredCustomers = computed(() => {
    const search =
      this.searchText()
        .trim()
        .toLowerCase();

    const status =
      this.statusFilter();

    return [...this.customers()]
      .filter((customer) => {
        const searchMatches =
          search.length === 0
          || customer.fullName
            .toLowerCase()
            .includes(search)
          || customer.email
            .toLowerCase()
            .includes(search)
          || customer.phone
            .toLowerCase()
            .includes(search)
          || customer.customerId
            .toString()
            .includes(search)
          || customer.userId
            .toString()
            .includes(search);

        const statusMatches =
          status === 'ALL'
          || customer.status === status;

        return searchMatches && statusMatches;
      })
      .sort(
        (first, second) =>
          first.customerId - second.customerId,
      );
  });

  readonly activeCount = computed(() =>
    this.customers().filter(
      (customer) => customer.status === 'ACTIVE',
    ).length,
  );

  readonly suspendedCount = computed(() =>
    this.customers().filter(
      (customer) => customer.status === 'SUSPENDED',
    ).length,
  );

  ngOnInit(): void {
    this.loadCustomers();
  }

  reload(): void {
    this.loadCustomers();
  }

  updateSearch(value: string): void {
    this.searchText.set(value);
  }

  updateStatusFilter(value: string): void {
    this.statusFilter.set(
      value as CustomerStatusFilter,
    );
  }

  clearFilters(): void {
    this.searchText.set('');
    this.statusFilter.set('ALL');
  }

  toggleStatus(customer: AdminCustomer): void {
    this.error.set(null);
    this.success.set(null);

    const status: AdminCustomerStatus =
      customer.status === 'ACTIVE'
        ? 'SUSPENDED'
        : 'ACTIVE';

    this.updatingCustomerId.set(
      customer.customerId,
    );

    this.customerApi
      .updateCustomerStatus(
        customer.customerId,
        { status },
      )
      .pipe(
        finalize(() =>
          this.updatingCustomerId.set(null),
        ),
      )
      .subscribe({
        next: (updatedCustomer) => {
          this.customers.update((customers) =>
            customers.map((existingCustomer) =>
              existingCustomer.customerId
                === updatedCustomer.customerId
                ? updatedCustomer
                : existingCustomer,
            ),
          );

          this.success.set(
            `${updatedCustomer.fullName} is now ${this.formatLabel(
              updatedCustomer.status,
            )}.`,
          );
        },

        error: (error) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }

  formatLabel(value: string): string {
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

  private loadCustomers(): void {
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);

    this.customerApi
      .getAllCustomers()
      .pipe(
        finalize(() =>
          this.loading.set(false),
        ),
      )
      .subscribe({
        next: (customers) => {
          this.customers.set(customers);
        },

        error: (error) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }
}