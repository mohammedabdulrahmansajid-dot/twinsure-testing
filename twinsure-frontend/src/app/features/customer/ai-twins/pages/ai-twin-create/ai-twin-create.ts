// Registers a Customer-owned AI Twin using Reactive Forms.
// Frontend validation mirrors backend constraints, while NgRx
// performs the create request and manages loading and error state.

import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Store } from '@ngrx/store';

import { AutonomyLevel } from '../../models/ai-twin-types.model';
import { AiTwinActions } from '../../state/ai-twin.actions';
import { selectError, selectSaving } from '../../state/ai-twin.selectors';
import { financialControlsValidator } from '../../validators/financial-controls.validator';

type AiTwinControlName =
  | 'twinName'
  | 'providerName'
  | 'modelName'
  | 'autonomyLevel'
  | 'transactionLimit'
  | 'approvalThreshold';

interface AutonomyOption {
  value: AutonomyLevel;
  label: string;
}

@Component({
  selector: 'app-ai-twin-create',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './ai-twin-create.html',
  styleUrl: './ai-twin-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AiTwinCreate implements OnInit, OnDestroy {
  private readonly formBuilder = inject(FormBuilder);

  private readonly store = inject(Store);

  readonly saving = this.store.selectSignal(selectSaving);

  readonly error = this.store.selectSignal(selectError);

  readonly autonomyOptions: AutonomyOption[] = [
    {
      value: 'ASSISTIVE',
      label: 'Assistive',
    },
    {
      value: 'SUPERVISED',
      label: 'Supervised',
    },
    {
      value: 'AUTONOMOUS',
      label: 'Autonomous',
    },
  ];

  readonly aiTwinForm = this.formBuilder.nonNullable.group(
    {
      twinName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],

      providerName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],

      modelName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],

      autonomyLevel: ['' as AutonomyLevel | '', [Validators.required]],

      transactionLimit: [0, [Validators.required, Validators.min(0.01)]],

      approvalThreshold: [0, [Validators.required, Validators.min(0)]],
    },
    {
      validators: [financialControlsValidator],
    },
  );

  get thresholdExceedsLimit(): boolean {
    return (
      this.aiTwinForm.hasError('thresholdExceedsLimit') &&
      (this.aiTwinForm.controls.approvalThreshold.touched ||
        this.aiTwinForm.controls.approvalThreshold.dirty ||
        this.aiTwinForm.controls.transactionLimit.touched ||
        this.aiTwinForm.controls.transactionLimit.dirty)
    );
  }

  ngOnInit(): void {
    this.store.dispatch(AiTwinActions.clearError());
  }

  ngOnDestroy(): void {
    this.store.dispatch(AiTwinActions.clearError());
  }

  controlInvalid(controlName: AiTwinControlName): boolean {
    const control = this.aiTwinForm.controls[controlName];

    return control.invalid && (control.touched || control.dirty);
  }

  submit(): void {
    this.store.dispatch(AiTwinActions.clearError());

    if (this.aiTwinForm.invalid) {
      this.aiTwinForm.markAllAsTouched();

      return;
    }

    const formValue = this.aiTwinForm.getRawValue();

    if (formValue.autonomyLevel === '') {
      return;
    }

    this.store.dispatch(
      AiTwinActions.createAiTwin({
        request: {
          twinName: formValue.twinName.trim(),

          providerName: formValue.providerName.trim(),

          modelName: formValue.modelName.trim(),

          autonomyLevel: formValue.autonomyLevel,

          transactionLimit: Number(formValue.transactionLimit),

          approvalThreshold: Number(formValue.approvalThreshold),
        },
      }),
    );
  }
}
