// Displays and updates one Customer-owned AI Twin.
// Active-policy protection disables configuration and permission changes
// while backend validation remains the authoritative security control.

import {
  CurrencyPipe,
  DatePipe
} from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  OnDestroy,
  OnInit,
  signal
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';

import {
  Store
} from '@ngrx/store';

import {
  finalize
} from 'rxjs';

import {
  getApiErrorMessage
} from '../../../../../core/http/api-error.util';
import {
  AiTwinConfigurationLock
} from '../../models/ai-twin-configuration-lock.model';
import {
  ActionType,
  AutonomyLevel,
  PermissionLevel
} from '../../models/ai-twin-types.model';
import {
  TwinPermissionResponse
} from '../../models/twin-permission-response.model';
import {
  AiTwinApiService
} from '../../services/ai-twin-api.service';
import {
  AiTwinActions
} from '../../state/ai-twin.actions';
import {
  selectError,
  selectLoading,
  selectPermissions,
  selectPermissionsLoading,
  selectSaving,
  selectSelectedAiTwin
} from '../../state/ai-twin.selectors';
import {
  financialControlsValidator
} from '../../validators/financial-controls.validator';

type ConfigurationControlName =
  | 'autonomyLevel'
  | 'transactionLimit'
  | 'approvalThreshold';

interface SelectOption<T> {
  value: T;
  label: string;
}

@Component({
  selector: 'app-ai-twin-details',

  imports: [
    CurrencyPipe,
    DatePipe,
    ReactiveFormsModule,
    RouterLink
  ],

  templateUrl:
    './ai-twin-details.html',

  styleUrl:
    './ai-twin-details.css',

  changeDetection:
    ChangeDetectionStrategy.OnPush
})
export class AiTwinDetails
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly store =
    inject(Store);

  private readonly aiTwinApi =
    inject(AiTwinApiService);

  private readonly twinId =
    Number(
      this.route.snapshot.paramMap.get(
        'twinId'
      )
    );

  readonly aiTwin =
    this.store.selectSignal(
      selectSelectedAiTwin
    );

  readonly permissions =
    this.store.selectSignal(
      selectPermissions
    );

  readonly loading =
    this.store.selectSignal(
      selectLoading
    );

  readonly permissionsLoading =
    this.store.selectSignal(
      selectPermissionsLoading
    );

  readonly saving =
    this.store.selectSignal(
      selectSaving
    );

  readonly error =
    this.store.selectSignal(
      selectError
    );

  readonly policyLock =
    signal<AiTwinConfigurationLock | null>(
      null
    );

  readonly lockLoading =
    signal(true);

  readonly lockError =
    signal<string | null>(
      null
    );

  readonly editing =
    signal(false);

  readonly selectedActionType =
    signal<ActionType | null>(
      null
    );

  readonly actionTypes:
    ActionType[] = [
      'TRAVEL_BOOKING',
      'ONLINE_PURCHASE',
      'SUBSCRIPTION_MANAGEMENT'
    ];

  readonly autonomyOptions:
    SelectOption<AutonomyLevel>[] = [
      {
        value: 'ASSISTIVE',
        label: 'Assistive'
      },
      {
        value: 'SUPERVISED',
        label: 'Supervised'
      },
      {
        value: 'AUTONOMOUS',
        label: 'Autonomous'
      }
    ];

  readonly permissionOptions:
    SelectOption<PermissionLevel>[] = [
      {
        value: 'PROHIBITED',
        label: 'Prohibited'
      },
      {
        value: 'APPROVAL_REQUIRED',
        label: 'Approval required'
      },
      {
        value: 'ALLOWED',
        label: 'Allowed'
      }
    ];

  readonly configurationForm =
    this.formBuilder.nonNullable.group(
      {
        autonomyLevel: [
          'SUPERVISED' as AutonomyLevel,
          [
            Validators.required
          ]
        ],

        transactionLimit: [
          0,
          [
            Validators.required,
            Validators.min(0.01)
          ]
        ],

        approvalThreshold: [
          0,
          [
            Validators.required,
            Validators.min(0)
          ]
        ]
      },
      {
        validators: [
          financialControlsValidator
        ]
      }
    );

  readonly permissionForm =
    this.formBuilder.group({
      permissionLevel: [
        'ALLOWED' as PermissionLevel,
        [
          Validators.required
        ]
      ],

      actionLimit: [
        null as number | null,
        [
          Validators.min(0.01)
        ]
      ]
    });

  get thresholdExceedsLimit(): boolean {

    return this.configurationForm
      .hasError(
        'thresholdExceedsLimit'
      )
      && (
        this.configurationForm.controls
          .transactionLimit.touched
        || this.configurationForm.controls
          .approvalThreshold.touched
      );
  }

  get configurationLocked(): boolean {

    return this.policyLock()
      ?.locked
      ?? false;
  }

  constructor() {

    effect(() => {

      const aiTwin =
        this.aiTwin();

      if (
        aiTwin !== null
        && this.editing()
      ) {
        this.populateConfigurationForm();
      }
    });

    this.permissionForm.controls
      .permissionLevel
      .valueChanges
      .subscribe(permissionLevel => {

        this.updateActionLimitState(
          permissionLevel
          ?? 'ALLOWED'
        );
      });
  }

  ngOnInit(): void {

    if (
      !Number.isInteger(
        this.twinId
      )
      || this.twinId <= 0
    ) {
      this.router.navigateByUrl(
        '/customer/ai-twins'
      );

      return;
    }

    this.store.dispatch(
      AiTwinActions.loadAiTwinDetails({
        twinId: this.twinId
      })
    );

    this.store.dispatch(
      AiTwinActions.loadPermissions({
        twinId: this.twinId
      })
    );

    this.loadConfigurationLock();
  }

  ngOnDestroy(): void {

    this.store.dispatch(
      AiTwinActions.clearSelectedAiTwin()
    );
  }

  controlInvalid(
    controlName: ConfigurationControlName
  ): boolean {

    const control =
      this.configurationForm.controls[
        controlName
      ];

    return control.invalid
      && (
        control.touched
        || control.dirty
      );
  }

  reloadLock(): void {

    this.loadConfigurationLock();
  }

  startEditing(): void {

    if (this.configurationLocked) {
      return;
    }

    this.store.dispatch(
      AiTwinActions.clearError()
    );

    this.editing.set(
      true
    );

    this.populateConfigurationForm();
  }

  cancelEditing(): void {

    this.editing.set(
      false
    );

    this.populateConfigurationForm();

    this.store.dispatch(
      AiTwinActions.clearError()
    );
  }

  saveConfiguration(): void {

    if (this.configurationLocked) {
      return;
    }

    this.store.dispatch(
      AiTwinActions.clearError()
    );

    if (
      this.configurationForm.invalid
    ) {
      this.configurationForm
        .markAllAsTouched();

      return;
    }

    const value =
      this.configurationForm
        .getRawValue();

    this.store.dispatch(
      AiTwinActions.updateAiTwin({
        twinId: this.twinId,

        request: {
          autonomyLevel:
            value.autonomyLevel,

          transactionLimit:
            Number(
              value.transactionLimit
            ),

          approvalThreshold:
            Number(
              value.approvalThreshold
            )
        }
      })
    );

    this.editing.set(
      false
    );
  }

  permissionFor(
    actionType: ActionType
  ): TwinPermissionResponse | undefined {

    return this.permissions()
      .find(
        permission =>
          permission.actionType
            === actionType
      );
  }

  editPermission(
    actionType: ActionType
  ): void {

    if (this.configurationLocked) {
      return;
    }

    this.store.dispatch(
      AiTwinActions.clearError()
    );

    this.selectedActionType.set(
      actionType
    );

    const existingPermission =
      this.permissionFor(
        actionType
      );

    const permissionLevel =
      existingPermission
        ?.permissionLevel
      ?? 'ALLOWED';

    this.permissionForm.reset({
      permissionLevel,

      actionLimit:
        existingPermission
          ?.actionLimit
        ?? null
    });

    this.updateActionLimitState(
      permissionLevel
    );
  }

  cancelPermission(): void {

    this.selectedActionType.set(
      null
    );

    this.permissionForm.reset({
      permissionLevel: 'ALLOWED',
      actionLimit: null
    });

    this.store.dispatch(
      AiTwinActions.clearError()
    );
  }

  savePermission(): void {

    if (this.configurationLocked) {
      return;
    }

    const actionType =
      this.selectedActionType();

    if (actionType === null) {
      return;
    }

    if (this.permissionForm.invalid) {

      this.permissionForm
        .markAllAsTouched();

      return;
    }

    const value =
      this.permissionForm
        .getRawValue();

    const actionLimit =
      value.permissionLevel
        === 'PROHIBITED'
        ? null
        : value.actionLimit === null
          ? null
          : Number(
              value.actionLimit
            );

    this.store.dispatch(
      AiTwinActions.configurePermission({
        twinId: this.twinId,

        request: {
          actionType,

          permissionLevel:
            value.permissionLevel
            ?? 'ALLOWED',

          actionLimit
        }
      })
    );

    this.selectedActionType.set(
      null
    );
  }

  formatLabel(
    value: string
  ): string {

    return value
      .toLowerCase()
      .split('_')
      .map(part =>
        part.charAt(0).toUpperCase()
        + part.slice(1)
      )
      .join(' ');
  }

  private loadConfigurationLock(): void {

    this.lockLoading.set(
      true
    );

    this.lockError.set(
      null
    );

    this.aiTwinApi
      .getConfigurationLock(
        this.twinId
      )
      .pipe(
        finalize(() =>
          this.lockLoading.set(
            false
          )
        )
      )
      .subscribe({
        next: policyLock => {

          this.policyLock.set(
            policyLock
          );

          if (policyLock.locked) {

            this.editing.set(
              false
            );

            this.selectedActionType.set(
              null
            );
          }
        },

        error: error => {

          this.lockError.set(
            getApiErrorMessage(
              error
            )
          );

          // Fail closed if lock status cannot be verified.
          this.policyLock.set({
            twinId: this.twinId,
            locked: true,
            policyId: null,
            policyStatus: null,
            policyStartDate: null,
            policyEndDate: null,
            explanation:
              'Configuration changes are unavailable because policy protection could not be verified.'
          });
        }
      });
  }

  private populateConfigurationForm(): void {

    const aiTwin =
      this.aiTwin();

    if (aiTwin === null) {
      return;
    }

    this.configurationForm.reset({
      autonomyLevel:
        aiTwin.autonomyLevel,

      transactionLimit:
        aiTwin.transactionLimit,

      approvalThreshold:
        aiTwin.approvalThreshold
    });
  }

  private updateActionLimitState(
    permissionLevel: PermissionLevel
  ): void {

    const actionLimitControl =
      this.permissionForm.controls
        .actionLimit;

    if (
      permissionLevel
        === 'PROHIBITED'
    ) {
      actionLimitControl.disable({
        emitEvent: false
      });

      actionLimitControl.setValue(
        null
      );

      return;
    }

    actionLimitControl.enable({
      emitEvent: false
    });
  }
}