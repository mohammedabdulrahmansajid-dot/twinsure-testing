// Creates a formal claim from an owned reported incident.
// The incident supplies ownership and policy context, while the
// Customer provides the requested compensation amount.

import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { Incident } from '../../../incidents/models/incident.model';
import { IncidentApiService } from '../../../incidents/services/incident-api.service';
import { ClaimApiService } from '../../services/claim-api.service';

@Component({
  selector: 'app-claim-create',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './claim-create.html',
  styleUrl: './claim-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClaimCreate implements OnInit {
  private readonly formBuilder = inject(FormBuilder);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly incidentApi = inject(IncidentApiService);

  private readonly claimApi = inject(ClaimApiService);

  readonly incidentId = Number(this.route.snapshot.queryParamMap.get('incidentId'));

  readonly incident = signal<Incident | null>(null);

  readonly loading = signal(true);

  readonly submitting = signal(false);

  readonly error = signal<string | null>(null);

  readonly claimForm = this.formBuilder.group({
    claimedAmount: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  get claimedAmountInvalid(): boolean {
    const control = this.claimForm.controls.claimedAmount;

    return control.invalid && (control.touched || control.dirty);
  }

  ngOnInit(): void {
    if (!Number.isInteger(this.incidentId) || this.incidentId <= 0) {
      this.router.navigateByUrl('/customer/incidents');

      return;
    }

    this.loadIncident();
  }

  submit(): void {
    this.error.set(null);

    if (this.claimForm.invalid || this.incident() === null) {
      this.claimForm.markAllAsTouched();

      return;
    }

    const claimedAmount = this.claimForm.controls.claimedAmount.value;

    if (claimedAmount === null || claimedAmount === undefined) {
      return;
    }

    this.submitting.set(true);

    this.claimApi
      .createClaim({
        incidentId: this.incidentId,

        claimedAmount: Number(claimedAmount),
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (claim) => {
          this.router.navigate(['/customer/claims', claim.claimId]);
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
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
        next: (details) => {
          if (details.incident.status !== 'REPORTED') {
            this.error.set('A claim has already been created ' + 'or this incident is closed.');

            return;
          }

          this.incident.set(details.incident);

          this.claimForm.controls.claimedAmount.setValue(details.incident.lossAmount);
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }
}
