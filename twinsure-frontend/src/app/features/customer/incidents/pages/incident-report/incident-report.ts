// Reports an incident from a detected claim-eligible AI Action violation.
// Incident choices are derived from action evidence instead of allowing
// the Customer to select an unrelated or manufactured violation type.

import { CurrencyPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { AiActionDetails } from '../../../ai-actions/models/ai-action-details.model';
import { ViolationType } from '../../../ai-actions/models/ai-action-types.model';
import { AiActionApiService } from '../../../ai-actions/services/ai-action-api.service';
import { IncidentType } from '../../models/incident-types.model';
import { IncidentApiService } from '../../services/incident-api.service';

type IncidentControlName = 'incidentType' | 'description' | 'lossAmount';

@Component({
  selector: 'app-incident-report',

  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink],

  templateUrl: './incident-report.html',

  styleUrl: './incident-report.css',

  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IncidentReport implements OnInit {
  private readonly formBuilder = inject(FormBuilder);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly actionApi = inject(AiActionApiService);

  private readonly incidentApi = inject(IncidentApiService);

  readonly actionId = Number(this.route.snapshot.queryParamMap.get('actionId'));

  readonly action = signal<AiActionDetails | null>(null);

  readonly loading = signal(true);

  readonly submitting = signal(false);

  readonly error = signal<string | null>(null);

  readonly eligibleIncidentTypes = computed<IncidentType[]>(() => {
    const action = this.action();

    if (action === null) {
      return [];
    }

    const supportedTypes = new Set<IncidentType>();

    action.violations
      .filter((violation) => violation.claimEligible)
      .forEach((violation) => {
        if (this.isIncidentType(violation.violationType)) {
          supportedTypes.add(violation.violationType);
        }
      });

    return Array.from(supportedTypes);
  });

  readonly hasEligibleViolations = computed(() => this.eligibleIncidentTypes().length > 0);

  readonly selectedViolation = computed(() => {
    const selectedType = this.incidentForm.controls.incidentType.value;

    if (selectedType === null || selectedType === '') {
      return null;
    }

    return (
      this.action()?.violations.find(
        (violation) => violation.claimEligible && violation.violationType === selectedType,
      ) ?? null
    );
  });

  readonly incidentForm = this.formBuilder.group({
    incidentType: ['' as IncidentType | '', [Validators.required]],

    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],

    lossAmount: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  ngOnInit(): void {
    if (!Number.isInteger(this.actionId) || this.actionId <= 0) {
      this.router.navigateByUrl('/customer/ai-actions');

      return;
    }

    this.loadAction();
  }

  controlInvalid(controlName: IncidentControlName): boolean {
    const control = this.incidentForm.controls[controlName];

    return control.invalid && (control.touched || control.dirty);
  }

  submit(): void {
    this.error.set(null);

    const action = this.action();

    if (this.incidentForm.invalid || action === null) {
      this.incidentForm.markAllAsTouched();

      return;
    }

    const value = this.incidentForm.getRawValue();

    if (
      value.incidentType === null ||
      value.incidentType === '' ||
      value.lossAmount === null ||
      value.lossAmount === undefined ||
      action.policyId === null
    ) {
      return;
    }

    if (!this.eligibleIncidentTypes().includes(value.incidentType)) {
      this.error.set(
        'The selected incident type is not present in the evaluated AI action evidence.',
      );

      this.incidentForm.controls.incidentType.setValue('');

      return;
    }

    const actionAmount = action.actionAmount;

    const lossAmount = Number(value.lossAmount);

    if (actionAmount !== null && lossAmount > actionAmount) {
      this.error.set('Reported loss cannot exceed the simulated action amount.');

      return;
    }

    this.submitting.set(true);

    this.incidentApi
      .reportIncident({
        actionId: action.actionId,

        policyId: action.policyId,

        incidentType: value.incidentType,

        description: value.description?.trim() ?? '',

        lossAmount,
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (incident) => {
          this.router.navigate(['/customer/incidents', incident.incidentId]);
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

  private loadAction(): void {
    this.loading.set(true);

    this.error.set(null);

    this.actionApi
      .getActionDetails(this.actionId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (action) => {
          this.action.set(action);

          const eligibleTypes = Array.from(
            new Set(
              action.violations
                .filter((violation) => violation.claimEligible)
                .map((violation) => violation.violationType)
                .filter((violationType) => this.isIncidentType(violationType)),
            ),
          );

          if (eligibleTypes.length === 1) {
            this.incidentForm.controls.incidentType.setValue(eligibleTypes[0]);
          } else {
            this.incidentForm.controls.incidentType.setValue('');
          }
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  private isIncidentType(violationType: ViolationType): violationType is IncidentType {
    return [
      'WRONG_ACTION',
      'APPROVAL_MISSING',
      'LIMIT_EXCEEDED',
      'DUPLICATE_ACTION',
      'MISSED_CANCELLATION',
      'PROHIBITED_ACTION',
    ].includes(violationType);
  }
}
