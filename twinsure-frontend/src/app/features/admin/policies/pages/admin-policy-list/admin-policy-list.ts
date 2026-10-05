// Displays every issued policy and supports administrative deactivation.
// ACTIVE policies may be cancelled or expired with a required reason.

import { CurrencyPipe, DatePipe } from '@angular/common';
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
  AdminPolicy,
  AdminPolicyStatus,
} from '../../models/admin-policy.model';
import { AdminPolicyApiService } from '../../services/admin-policy-api.service';

type PolicyStatusFilter =
  | 'ALL'
  | AdminPolicyStatus;

type PolicyEndStatus =
  | 'CANCELLED'
  | 'EXPIRED';

@Component({
  selector: 'app-admin-policy-list',
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
    RouterLink,
  ],
  templateUrl: './admin-policy-list.html',
  styleUrl: './admin-policy-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPolicyList implements OnInit {
  private readonly policyApi =
    inject(AdminPolicyApiService);

  readonly policies =
    signal<AdminPolicy[]>([]);

  readonly loading = signal(true);

  readonly updating = signal(false);

  readonly error =
    signal<string | null>(null);

  readonly success =
    signal<string | null>(null);

  readonly searchText = signal('');

  readonly statusFilter =
    signal<PolicyStatusFilter>('ALL');

  readonly selectedPolicyId =
    signal<number | null>(null);

  readonly selectedEndStatus =
    signal<PolicyEndStatus>('CANCELLED');

  readonly statusReason = signal('');

  readonly statusOptions: PolicyStatusFilter[] = [
    'ALL',
    'ACTIVE',
    'CANCELLED',
    'EXPIRED',
  ];

  readonly filteredPolicies = computed(() => {
    const search =
      this.searchText()
        .trim()
        .toLowerCase();

    const selectedStatus =
      this.statusFilter();

    return [...this.policies()]
      .filter((policy) => {
        const searchMatches =
          search.length === 0
          || policy.policyNumber
            .toLowerCase()
            .includes(search)
          || policy.policyId
            .toString()
            .includes(search)
          || policy.applicationId
            .toString()
            .includes(search)
          || policy.customerId
            .toString()
            .includes(search)
          || policy.twinId
            .toString()
            .includes(search)
          || policy.productId
            .toString()
            .includes(search);

        const statusMatches =
          selectedStatus === 'ALL'
          || policy.status === selectedStatus;

        return searchMatches && statusMatches;
      })
      .sort(
        (first, second) =>
          new Date(second.startDate).getTime()
          - new Date(first.startDate).getTime(),
      );
  });

  readonly activeCount = computed(() =>
    this.policies().filter(
      (policy) => policy.status === 'ACTIVE',
    ).length,
  );

  readonly inactiveCount = computed(() =>
    this.policies().filter(
      (policy) => policy.status !== 'ACTIVE',
    ).length,
  );

  ngOnInit(): void {
    this.loadPolicies();
  }

  reload(): void {
    this.loadPolicies();
  }

  updateSearch(value: string): void {
    this.searchText.set(value);
  }

  updateStatusFilter(value: string): void {
    this.statusFilter.set(
      value as PolicyStatusFilter,
    );
  }

  clearFilters(): void {
    this.searchText.set('');
    this.statusFilter.set('ALL');
  }

  openStatusUpdate(
    policy: AdminPolicy,
  ): void {
    if (policy.status !== 'ACTIVE') {
      return;
    }

    this.error.set(null);
    this.success.set(null);
    this.selectedPolicyId.set(policy.policyId);
    this.selectedEndStatus.set('CANCELLED');
    this.statusReason.set('');
  }

  cancelStatusUpdate(): void {
    this.selectedPolicyId.set(null);
    this.statusReason.set('');
  }

  updateEndStatus(value: string): void {
    this.selectedEndStatus.set(
      value as PolicyEndStatus,
    );
  }

  updateReason(value: string): void {
    this.statusReason.set(value);
  }

  saveStatus(
    policy: AdminPolicy,
  ): void {
    const reason =
      this.statusReason().trim();

    if (policy.status !== 'ACTIVE') {
      this.error.set(
        'Only an active policy can be cancelled or expired.',
      );

      return;
    }

    if (
      reason.length < 5
      || reason.length > 500
    ) {
      this.error.set(
        'Reason must contain 5 to 500 characters.',
      );

      return;
    }

    this.error.set(null);
    this.success.set(null);
    this.updating.set(true);

    this.policyApi
      .updatePolicyStatus(
        policy.policyId,
        {
          status: this.selectedEndStatus(),
          reason,
        },
      )
      .pipe(
        finalize(() =>
          this.updating.set(false),
        ),
      )
      .subscribe({
        next: (updatedPolicy) => {
          this.policies.update((policies) =>
            policies.map((existingPolicy) =>
              existingPolicy.policyId
                === updatedPolicy.policyId
                ? updatedPolicy
                : existingPolicy,
            ),
          );

          this.selectedPolicyId.set(null);
          this.statusReason.set('');

          this.success.set(
            `Policy ${updatedPolicy.policyNumber} is now ${this.formatLabel(
              updatedPolicy.status,
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

  statusClasses(
    status: AdminPolicyStatus,
  ): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-100 text-emerald-700';

      case 'CANCELLED':
        return 'bg-red-100 text-red-700';

      case 'EXPIRED':
        return 'bg-slate-200 text-slate-700';

      default:
        return 'bg-blue-100 text-blue-700';
    }
  }

  private loadPolicies(): void {
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);
    this.selectedPolicyId.set(null);

    this.policyApi
      .getAllPolicies()
      .pipe(
        finalize(() =>
          this.loading.set(false),
        ),
      )
      .subscribe({
        next: (policies) => {
          this.policies.set(policies);
        },

        error: (error) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }
}