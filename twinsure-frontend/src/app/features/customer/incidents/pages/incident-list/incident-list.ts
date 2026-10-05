// Displays incidents reported by the authenticated Customer.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { Incident } from '../../models/incident.model';
import { IncidentApiService } from '../../services/incident-api.service';

@Component({
  selector: 'app-incident-list',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './incident-list.html',
  styleUrl: './incident-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IncidentList implements OnInit {
  private readonly incidentApi = inject(IncidentApiService);

  readonly incidents = signal<Incident[]>([]);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  readonly loadingItems = [1, 2, 3];

  ngOnInit(): void {
    this.loadIncidents();
  }

  loadIncidents(): void {
    this.loading.set(true);
    this.error.set(null);

    this.incidentApi
      .getMyIncidents()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (incidents) =>
          this.incidents.set(
            [...incidents].sort(
              (first, second) =>
                new Date(second.reportedAt).getTime() - new Date(first.reportedAt).getTime(),
            ),
          ),

        error: (error) => this.error.set(getApiErrorMessage(error)),
      });
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }
}
