// Displays all policy applications for Administrator oversight.
// Search, status filtering, and latest-activity ordering run locally.

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

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import {
  AdminPolicyApplication,
  AdminPolicyApplicationStatus,
} from '../../models/admin-policy-application.model';
import { AdminPolicyApplicationApiService } from '../../services/admin-policy-application-api.service';

type ApplicationStatusFilter =
  | 'ALL'
  | AdminPolicyApplicationStatus;

@Component({
  selector: 'app-admin-policy-application-list',
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
  ],
  templateUrl: './admin-policy-application-list.html',
  styleUrl: './admin-policy-application-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPolicyApplicationList implements OnInit {
  private readonly applicationApi =
    inject(AdminPolicyApplicationApiService);

  readonly applications =
    signal<AdminPolicyApplication[]>([]);

  readonly loading = signal(true);

  readonly error =
    signal<string | null>(null);

  readonly searchText = signal('');

  readonly statusFilter =
    signal<ApplicationStatusFilter>('ALL');

  readonly statusOptions: ApplicationStatusFilter[] = [
    'ALL',
    'SUBMITTED',
    'UNDER_REVIEW',
    'CHANGES_REQUESTED',
    'APPROVED',
    'REJECTED',
    'ACCEPTED',
    'DECLINED',
    'EXPIRED',
  ];

  readonly filteredApplications = computed(() => {
    const search =
      this.searchText()
        .trim()
        .toLowerCase();

    const selectedStatus =
      this.statusFilter();

    return [...this.applications()]
      .filter((application) => {
        const searchMatches =
          search.length === 0
          || application.applicationId
            .toString()
            .includes(search)
          || application.customerId
            .toString()
            .includes(search)
          || application.twinId
            .toString()
            .includes(search)
          || application.productId
            .toString()
            .includes(search)
          || (
            application.reviewedBy !== null
            && application.reviewedBy
              .toString()
              .includes(search)
          );

        const statusMatches =
          selectedStatus === 'ALL'
          || application.status === selectedStatus;

        return searchMatches && statusMatches;
      })
      .sort(
        (first, second) =>
          this.activityTime(second)
          - this.activityTime(first),
      );
  });

  readonly pendingCount = computed(() =>
    this.applications().filter(
      (application) =>
        application.status === 'SUBMITTED'
        || application.status === 'UNDER_REVIEW'
        || application.status === 'CHANGES_REQUESTED',
    ).length,
  );

  readonly approvedCount = computed(() =>
    this.applications().filter(
      (application) =>
        application.status === 'APPROVED'
        || application.status === 'ACCEPTED',
    ).length,
  );

  readonly rejectedCount = computed(() =>
    this.applications().filter(
      (application) =>
        application.status === 'REJECTED'
        || application.status === 'DECLINED'
        || application.status === 'EXPIRED',
    ).length,
  );

  ngOnInit(): void {
    this.loadApplications();
  }

  reload(): void {
    this.loadApplications();
  }

  updateSearch(value: string): void {
    this.searchText.set(value);
  }

  updateStatusFilter(value: string): void {
    this.statusFilter.set(
      value as ApplicationStatusFilter,
    );
  }

  clearFilters(): void {
    this.searchText.set('');
    this.statusFilter.set('ALL');
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
    status: AdminPolicyApplicationStatus,
  ): string {
    switch (status) {
      case 'SUBMITTED':
        return 'bg-amber-100 text-amber-800';

      case 'UNDER_REVIEW':
      case 'CHANGES_REQUESTED':
        return 'bg-violet-100 text-violet-700';

      case 'APPROVED':
      case 'ACCEPTED':
        return 'bg-emerald-100 text-emerald-700';

      case 'REJECTED':
      case 'DECLINED':
        return 'bg-red-100 text-red-700';

      case 'EXPIRED':
        return 'bg-slate-200 text-slate-700';

      default:
        return 'bg-blue-100 text-blue-700';
    }
  }

  private activityTime(
    application: AdminPolicyApplication,
  ): number {
    const activityDate =
      application.acceptedAt
      ?? application.reviewedAt
      ?? application.submittedAt;

    return new Date(activityDate).getTime();
  }

  private loadApplications(): void {
    this.loading.set(true);
    this.error.set(null);

    this.applicationApi
      .getAllApplications()
      .pipe(
        finalize(() =>
          this.loading.set(false),
        ),
      )
      .subscribe({
        next: (applications) => {
          this.applications.set(applications);
        },

        error: (error) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }
}