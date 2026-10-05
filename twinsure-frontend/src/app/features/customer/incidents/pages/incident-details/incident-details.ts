// Displays one Customer-owned incident with validated AI Action evidence.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { IncidentDetails as IncidentDetailsModel } from '../../models/incident-details.model';
import { IncidentApiService } from '../../services/incident-api.service';

@Component({
  selector: 'app-incident-details',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './incident-details.html',
  styleUrl: './incident-details.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IncidentDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly incidentApi = inject(IncidentApiService);

  private readonly incidentId = Number(this.route.snapshot.paramMap.get('incidentId'));

  readonly details = signal<IncidentDetailsModel | null>(null);

  readonly loading = signal(true);

  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    if (!Number.isInteger(this.incidentId) || this.incidentId <= 0) {
      this.router.navigateByUrl('/customer/incidents');

      return;
    }

    this.loadIncident();
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private loadIncident(): void {
    this.loading.set(true);
    this.error.set(null);

    this.incidentApi
      .getIncidentDetails(this.incidentId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (details) => this.details.set(details),

        error: (error) => this.error.set(getApiErrorMessage(error)),
      });
  }
}
