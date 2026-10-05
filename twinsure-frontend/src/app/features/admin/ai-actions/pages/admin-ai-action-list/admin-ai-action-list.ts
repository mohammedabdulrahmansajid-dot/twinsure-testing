// Displays all evaluated AI actions for Administrator oversight.
// Search, status, action-type, and insurance filters run locally.

import {
  CurrencyPipe,
  DatePipe,
} from '@angular/common';
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
  AdminActionStatus,
  AdminActionType,
  AdminAiAction,
} from '../../models/admin-ai-action.model';
import { AdminAiActionApiService } from '../../services/admin-ai-action-api.service';

type ActionStatusFilter =
  | 'ALL'
  | AdminActionStatus;

type ActionTypeFilter =
  | 'ALL'
  | AdminActionType;

type InsuranceFilter =
  | 'ALL'
  | 'INSURED'
  | 'UNINSURED';

@Component({
  selector: 'app-admin-ai-action-list',
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
  ],
  templateUrl: './admin-ai-action-list.html',
  styleUrl: './admin-ai-action-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAiActionList implements OnInit {
  private readonly actionApi =
    inject(AdminAiActionApiService);

  readonly actions =
    signal<AdminAiAction[]>([]);

  readonly loading =
    signal(true);

  readonly error =
    signal<string | null>(null);

  readonly searchText =
    signal('');

  readonly statusFilter =
    signal<ActionStatusFilter>('ALL');

  readonly typeFilter =
    signal<ActionTypeFilter>('ALL');

  readonly insuranceFilter =
    signal<InsuranceFilter>('ALL');

  readonly statusOptions: ActionStatusFilter[] = [
    'ALL',
    'COMPLIANT',
    'VIOLATION_DETECTED',
  ];

  readonly typeOptions: ActionTypeFilter[] = [
    'ALL',
    'TRAVEL_BOOKING',
    'ONLINE_PURCHASE',
    'SUBSCRIPTION_MANAGEMENT',
  ];

  readonly insuranceOptions: InsuranceFilter[] = [
    'ALL',
    'INSURED',
    'UNINSURED',
  ];

  readonly filteredActions = computed(() => {
    const search =
      this.searchText()
        .trim()
        .toLowerCase();

    const selectedStatus =
      this.statusFilter();

    const selectedType =
      this.typeFilter();

    const selectedInsurance =
      this.insuranceFilter();

    return [...this.actions()]
      .filter((action) => {
        const searchMatches =
          search.length === 0
          || action.transactionReference
            .toLowerCase()
            .includes(search)
          || action.description
            .toLowerCase()
            .includes(search)
          || action.actionId
            .toString()
            .includes(search)
          || action.customerId
            .toString()
            .includes(search)
          || action.twinId
            .toString()
            .includes(search)
          || (
            action.policyId !== null
            && action.policyId
              .toString()
              .includes(search)
          );

        const statusMatches =
          selectedStatus === 'ALL'
          || action.actionStatus === selectedStatus;

        const typeMatches =
          selectedType === 'ALL'
          || action.actionType === selectedType;

        const insuranceMatches =
          selectedInsurance === 'ALL'
          || (
            selectedInsurance === 'INSURED'
            && action.insured
          )
          || (
            selectedInsurance === 'UNINSURED'
            && !action.insured
          );

        return searchMatches
          && statusMatches
          && typeMatches
          && insuranceMatches;
      })
      .sort(
        (first, second) =>
          new Date(second.evaluatedAt).getTime()
          - new Date(first.evaluatedAt).getTime(),
      );
  });

  readonly violationCount = computed(() =>
    this.actions().filter(
      (action) =>
        action.actionStatus === 'VIOLATION_DETECTED',
    ).length,
  );

  readonly compliantCount = computed(() =>
    this.actions().filter(
      (action) =>
        action.actionStatus === 'COMPLIANT',
    ).length,
  );

  readonly insuredCount = computed(() =>
    this.actions().filter(
      (action) => action.insured,
    ).length,
  );

  ngOnInit(): void {
    this.loadActions();
  }

  reload(): void {
    this.loadActions();
  }

  updateSearch(value: string): void {
    this.searchText.set(value);
  }

  updateStatusFilter(value: string): void {
    this.statusFilter.set(
      value as ActionStatusFilter,
    );
  }

  updateTypeFilter(value: string): void {
    this.typeFilter.set(
      value as ActionTypeFilter,
    );
  }

  updateInsuranceFilter(value: string): void {
    this.insuranceFilter.set(
      value as InsuranceFilter,
    );
  }

  clearFilters(): void {
    this.searchText.set('');
    this.statusFilter.set('ALL');
    this.typeFilter.set('ALL');
    this.insuranceFilter.set('ALL');
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

  statusClasses(status: AdminActionStatus): string {
    switch (status) {
      case 'COMPLIANT':
        return 'bg-emerald-100 text-emerald-700';

      case 'VIOLATION_DETECTED':
        return 'bg-red-100 text-red-700';

      default:
        return 'bg-slate-100 text-slate-700';
    }
  }

  scenarioSummary(action: AdminAiAction): string {
    switch (action.actionType) {
      case 'TRAVEL_BOOKING':
        return action.bookingOutcome === null
          ? 'No booking outcome'
          : `Booking ${this.formatLabel(action.bookingOutcome)}`;

      case 'ONLINE_PURCHASE':
        return action.purchaseOutcome === null
          ? 'No purchase outcome'
          : `Purchase ${this.formatLabel(action.purchaseOutcome)}`;

      case 'SUBSCRIPTION_MANAGEMENT':
        if (action.subscriptionOperation === null) {
          return 'No subscription operation';
        }

        if (
          action.subscriptionOperation === 'CANCELLED'
          && action.cancellationCompleted !== null
        ) {
          return action.cancellationCompleted
            ? 'Subscription cancellation completed'
            : 'Subscription cancellation not completed';
        }

        return `Subscription ${this.formatLabel(
          action.subscriptionOperation,
        )}`;

      default:
        return 'Scenario unavailable';
    }
  }

  private loadActions(): void {
    this.loading.set(true);
    this.error.set(null);

    this.actionApi
      .getAllActions()
      .pipe(
        finalize(() =>
          this.loading.set(false),
        ),
      )
      .subscribe({
        next: (actions) => {
          this.actions.set(actions);
        },

        error: (error: unknown) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }
}