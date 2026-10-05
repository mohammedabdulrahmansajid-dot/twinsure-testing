// Displays all reported incidents for Administrator oversight.
// Search, incident-type, and status filters run locally while
// Claims Service remains authoritative for incident lifecycle data.

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
import { AdminIncident } from '../../models/admin-incident.model';
import { AdminIncidentApiService } from '../../services/admin-incident-api.service';

@Component({
  selector: 'app-admin-incident-list',
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
  ],
  templateUrl: './admin-incident-list.html',
  styleUrl: './admin-incident-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminIncidentList implements OnInit {
  private readonly incidentApi =
    inject(AdminIncidentApiService);

  readonly incidents =
    signal<AdminIncident[]>([]);

  readonly loading =
    signal(true);

  readonly error =
    signal<string | null>(null);

  readonly searchText =
    signal('');

  readonly statusFilter =
    signal('ALL');

  readonly typeFilter =
    signal('ALL');

  readonly statusOptions = computed(() => [
    'ALL',
    ...Array.from(
      new Set(
        this.incidents().map(
          (incident) => incident.status,
        ),
      ),
    ).sort(),
  ]);

  readonly typeOptions = computed(() => [
    'ALL',
    ...Array.from(
      new Set(
        this.incidents().map(
          (incident) => incident.incidentType,
        ),
      ),
    ).sort(),
  ]);

  readonly filteredIncidents = computed(() => {
    const search =
      this.searchText()
        .trim()
        .toLowerCase();

    const selectedStatus =
      this.statusFilter();

    const selectedType =
      this.typeFilter();

    return [...this.incidents()]
      .filter((incident) => {
        const searchMatches =
          search.length === 0
          || incident.incidentId
            .toString()
            .includes(search)
          || incident.customerId
            .toString()
            .includes(search)
          || incident.twinId
            .toString()
            .includes(search)
          || incident.policyId
            .toString()
            .includes(search)
          || incident.actionId
            .toString()
            .includes(search)
          || incident.description
            .toLowerCase()
            .includes(search);

        const statusMatches =
          selectedStatus === 'ALL'
          || incident.status === selectedStatus;

        const typeMatches =
          selectedType === 'ALL'
          || incident.incidentType === selectedType;

        return searchMatches
          && statusMatches
          && typeMatches;
      })
      .sort(
        (first, second) =>
          new Date(
            second.reportedAt,
          ).getTime()
          - new Date(
            first.reportedAt,
          ).getTime(),
      );
  });

  readonly reportedCount = computed(() =>
    this.incidents().filter(
      (incident) =>
        incident.status === 'REPORTED',
    ).length,
  );

  readonly claimCreatedCount = computed(() =>
    this.incidents().filter(
      (incident) =>
        incident.status === 'CLAIM_CREATED',
    ).length,
  );

  readonly closedCount = computed(() =>
    this.incidents().filter(
      (incident) =>
        incident.status === 'CLOSED',
    ).length,
  );

  readonly totalReportedLoss = computed(() =>
    this.incidents().reduce(
      (total, incident) =>
        total + Number(incident.lossAmount),
      0,
    ),
  );

  ngOnInit(): void {
    this.loadIncidents();
  }

  reload(): void {
    this.loadIncidents();
  }

  updateSearch(
    value: string,
  ): void {
    this.searchText.set(value);
  }

  updateStatusFilter(
    value: string,
  ): void {
    this.statusFilter.set(value);
  }

  updateTypeFilter(
    value: string,
  ): void {
    this.typeFilter.set(value);
  }

  clearFilters(): void {
    this.searchText.set('');
    this.statusFilter.set('ALL');
    this.typeFilter.set('ALL');
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
    status: string,
  ): string {
    switch (status) {
      case 'REPORTED':
        return 'bg-amber-100 text-amber-800';

      case 'CLAIM_CREATED':
        return 'bg-blue-100 text-blue-700';

      case 'CLOSED':
        return 'bg-slate-200 text-slate-700';

      default:
        return 'bg-violet-100 text-violet-700';
    }
  }

  private loadIncidents(): void {
    this.loading.set(true);
    this.error.set(null);

    this.incidentApi
      .getAllIncidents()
      .pipe(
        finalize(() =>
          this.loading.set(false),
        ),
      )
      .subscribe({
        next: (incidents) => {
          this.incidents.set(incidents);
        },

        error: (error: unknown) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }
}