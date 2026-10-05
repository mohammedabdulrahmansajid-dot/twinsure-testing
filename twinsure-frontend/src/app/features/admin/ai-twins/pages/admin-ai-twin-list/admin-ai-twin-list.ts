// Displays all AI Twins for Administrator oversight.
// Admin can inspect financial controls and update operational status.
// AI Twin Service remains authoritative for permitted status changes.

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

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import {
  AdminAiTwin,
  AdminAiTwinStatus,
} from '../../models/admin-ai-twin.model';
import { AdminAiTwinApiService } from '../../services/admin-ai-twin-api.service';

type TwinStatusFilter =
  | 'ALL'
  | AdminAiTwinStatus;

@Component({
  selector: 'app-admin-ai-twin-list',
  imports: [
    CurrencyPipe,
    FormsModule,
  ],
  templateUrl: './admin-ai-twin-list.html',
  styleUrl: './admin-ai-twin-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAiTwinList implements OnInit {
  private readonly aiTwinApi =
    inject(AdminAiTwinApiService);

  readonly aiTwins =
    signal<AdminAiTwin[]>([]);

  readonly loading =
    signal(true);

  readonly error =
    signal<string | null>(null);

  readonly success =
    signal<string | null>(null);

  readonly updatingTwinId =
    signal<number | null>(null);

  readonly searchText =
    signal('');

  readonly statusFilter =
    signal<TwinStatusFilter>('ALL');

  readonly statusSelections =
    signal<Record<number, AdminAiTwinStatus>>({});

  readonly statusOptions: TwinStatusFilter[] = [
    'ALL',
    'ACTIVE',
    'SUSPENDED',
    'RETIRED',
  ];

  readonly filteredAiTwins =
    computed(() => {
      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const selectedFilter =
        this.statusFilter();

      return [...this.aiTwins()]
        .filter((aiTwin) => {
          const searchMatches =
            search.length === 0
            || aiTwin.twinName
              .toLowerCase()
              .includes(search)
            || aiTwin.providerName
              .toLowerCase()
              .includes(search)
            || aiTwin.modelName
              .toLowerCase()
              .includes(search)
            || aiTwin.twinId
              .toString()
              .includes(search)
            || aiTwin.customerId
              .toString()
              .includes(search);

          const statusMatches =
            selectedFilter === 'ALL'
            || aiTwin.status === selectedFilter;

          return searchMatches
            && statusMatches;
        })
        .sort(
          (first, second) =>
            first.twinId - second.twinId,
        );
    });

  readonly activeCount =
    computed(() =>
      this.aiTwins()
        .filter(
          (aiTwin) =>
            aiTwin.status === 'ACTIVE',
        )
        .length,
    );

  readonly restrictedCount =
    computed(() =>
      this.aiTwins()
        .filter(
          (aiTwin) =>
            aiTwin.status !== 'ACTIVE',
        )
        .length,
    );

  ngOnInit(): void {
    this.loadAiTwins();
  }

  reload(): void {
    this.loadAiTwins();
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
      value as TwinStatusFilter,
    );
  }

//  FIXED: Uses computed property syntax [twinId]
updateStatusSelection(
  twinId: number,
  value: string,
): void {
  const selectedStatus = value as AdminAiTwinStatus;
  this.statusSelections.update(
    (currentSelections) => ({
      ...currentSelections,
      [twinId]: selectedStatus, 
    }),
  );
}


  clearFilters(): void {
    this.searchText.set('');
    this.statusFilter.set('ALL');
  }

  selectedStatus(
    aiTwin: AdminAiTwin,
  ): AdminAiTwinStatus {
    return this.statusSelections()[
      aiTwin.twinId
    ] ?? aiTwin.status;
  }

  saveStatus(
    aiTwin: AdminAiTwin,
  ): void {
    this.error.set(null);
    this.success.set(null);

    const selectedStatus =
      this.selectedStatus(aiTwin);

    if (selectedStatus === aiTwin.status) {
      this.error.set(
        'Select a different AI Twin status.',
      );

      return;
    }

    this.updatingTwinId.set(
      aiTwin.twinId,
    );

    this.aiTwinApi
      .updateAiTwinStatus(
        aiTwin.twinId,
        {
          status: selectedStatus,
        },
      )
      .pipe(
        finalize(() =>
          this.updatingTwinId.set(null),
        ),
      )
      .subscribe({
        next: (updatedTwin) => {
          this.aiTwins.update(
            (currentAiTwins) =>
              currentAiTwins.map(
                (existingTwin) =>
                  existingTwin.twinId
                    === updatedTwin.twinId
                    ? updatedTwin
                    : existingTwin,
              ),
          );

          this.statusSelections.update(
            (currentSelections) => ({
              ...currentSelections,
              [updatedTwin.twinId]:
                updatedTwin.status,
            }),
          );

          this.success.set(
            `${updatedTwin.twinName} is now ${this.formatLabel(
              updatedTwin.status,
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

  private loadAiTwins(): void {
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);

    this.aiTwinApi
      .getAllAiTwins()
      .pipe(
        finalize(() =>
          this.loading.set(false),
        ),
      )
      .subscribe({
        next: (aiTwins) => {
          this.aiTwins.set(aiTwins);

          const selections:
            Record<number, AdminAiTwinStatus> = {};

          for (const aiTwin of aiTwins) {
            selections[aiTwin.twinId] =
              aiTwin.status;
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