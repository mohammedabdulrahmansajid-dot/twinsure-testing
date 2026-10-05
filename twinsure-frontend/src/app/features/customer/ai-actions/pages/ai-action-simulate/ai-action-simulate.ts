// Simulates an AI Twin action using common and action-specific facts.
// The backend derives violations from these facts and Twin configuration.

import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  Router
} from '@angular/router';

import {
  Store
} from '@ngrx/store';

import {
  finalize
} from 'rxjs';
import {
  takeUntilDestroyed
} from '@angular/core/rxjs-interop';

import {
  getApiErrorMessage
} from '../../../../../core/http/api-error.util';
import {
  AiTwinActions
} from '../../../ai-twins/state/ai-twin.actions';
import {
  selectActiveAiTwins,
  selectLoading as selectAiTwinsLoading
} from '../../../ai-twins/state/ai-twin.selectors';
import {
  ActionType,
  BookingOutcome,
  PurchaseOutcome,
  SubscriptionOperation
} from '../../models/ai-action-types.model';
import {
  AiActionApiService
} from '../../services/ai-action-api.service';

type ActionControlName =
  | 'twinId'
  | 'actionType'
  | 'transactionReference'
  | 'description'
  | 'actionAmount'
  | 'bookingOutcome'
  | 'purchaseOutcome'
  | 'subscriptionOperation'
  | 'cancellationCompleted';

@Component({
  selector: 'app-ai-action-simulate',

  imports: [
    ReactiveFormsModule
  ],

  templateUrl:
    './ai-action-simulate.html',

  styleUrl:
    './ai-action-simulate.css',

  changeDetection:
    ChangeDetectionStrategy.OnPush
})
export class AiActionSimulate
  implements OnInit {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly store =
    inject(Store);

  private readonly actionApi =
    inject(AiActionApiService);

  private readonly router =
    inject(Router);

  private readonly destroyRef =
    inject(DestroyRef);

  readonly activeAiTwins =
    this.store.selectSignal(
      selectActiveAiTwins
    );

  readonly twinsLoading =
    this.store.selectSignal(
      selectAiTwinsLoading
    );

  readonly submitting =
    signal(false);

  readonly error =
    signal<string | null>(
      null
    );

  readonly selectedActionType =
    signal<ActionType | null>(
      null
    );

  readonly selectedSubscriptionOperation =
    signal<SubscriptionOperation | null>(
      null
    );

  readonly actionTypes:
    ActionType[] = [
      'TRAVEL_BOOKING',
      'ONLINE_PURCHASE',
      'SUBSCRIPTION_MANAGEMENT'
    ];

  readonly bookingOutcomes:
    BookingOutcome[] = [
      'COMPLETED_CORRECTLY',
      'WRONG_BOOKING'
    ];

  readonly purchaseOutcomes:
    PurchaseOutcome[] = [
      'COMPLETED_CORRECTLY',
      'WRONG_PURCHASE',
      'DUPLICATE_PURCHASE'
    ];

  readonly subscriptionOperations:
    SubscriptionOperation[] = [
      'CREATE_OR_RENEW',
      'CANCEL'
    ];

  readonly actionForm =
    this.formBuilder.group({
      twinId: [
        null as number | null,
        [
          Validators.required
        ]
      ],

      actionType: [
        '' as ActionType | '',
        [
          Validators.required
        ]
      ],

      transactionReference: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(100)
        ]
      ],

      description: [
        '',
        [
          Validators.required,
          Validators.minLength(5),
          Validators.maxLength(500)
        ]
      ],

      actionAmount: [
        null as number | null,
        [
          Validators.min(0.01)
        ]
      ],

      approvalProvided: [
        false
      ],

      bookingOutcome: [
        null as BookingOutcome | null
      ],

      purchaseOutcome: [
        null as PurchaseOutcome | null
      ],

      subscriptionOperation: [
        null as SubscriptionOperation | null
      ],

      cancellationCompleted: [
        null as boolean | null
      ],

      occurredAt: [
        ''
      ]
    });

  ngOnInit(): void {

    this.store.dispatch(
      AiTwinActions.loadMyAiTwins()
    );

    this.actionForm.controls
      .actionType
      .valueChanges
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe(actionType => {

        const normalizedType =
          actionType === ''
            ? null
            : actionType;

        this.selectedActionType.set(
          normalizedType
        );

        this.configureScenarioControls(
          normalizedType
        );
      });

    this.actionForm.controls
      .subscriptionOperation
      .valueChanges
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe(operation => {

        this.selectedSubscriptionOperation.set(
          operation
        );

        this.configureCancellationControl(
          operation
        );
      });
  }

  controlInvalid(
    controlName: ActionControlName
  ): boolean {

    const control =
      this.actionForm.controls[
        controlName
      ];

    return control.invalid
      && (
        control.touched
        || control.dirty
      );
  }

  submit(): void {

    this.error.set(
      null
    );

    if (this.actionForm.invalid) {

      this.actionForm.markAllAsTouched();

      return;
    }

    const value =
      this.actionForm.getRawValue();

    if (
      value.twinId === null
      || value.actionType === ''
      || value.actionType === null
    ) {
      return;
    }

    const request =
      this.buildRequest(
        value.twinId,
        value.actionType
      );

    this.submitting.set(
      true
    );

    this.actionApi
      .simulateAction(
        request
      )
      .pipe(
        finalize(() =>
          this.submitting.set(
            false
          )
        )
      )
      .subscribe({
        next: response => {

          this.router.navigate([
            '/customer/ai-actions',
            response.actionId
          ]);
        },

        error: error => {

          this.error.set(
            getApiErrorMessage(
              error
            )
          );
        }
      });
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

  private configureScenarioControls(
    actionType: ActionType | null
  ): void {

    const bookingControl =
      this.actionForm.controls
        .bookingOutcome;

    const purchaseControl =
      this.actionForm.controls
        .purchaseOutcome;

    const subscriptionControl =
      this.actionForm.controls
        .subscriptionOperation;

    const cancellationControl =
      this.actionForm.controls
        .cancellationCompleted;

    bookingControl.clearValidators();
    purchaseControl.clearValidators();
    subscriptionControl.clearValidators();
    cancellationControl.clearValidators();

    bookingControl.setValue(
      null,
      {
        emitEvent: false
      }
    );

    purchaseControl.setValue(
      null,
      {
        emitEvent: false
      }
    );

    subscriptionControl.setValue(
      null
    );

    cancellationControl.setValue(
      null,
      {
        emitEvent: false
      }
    );

    this.selectedSubscriptionOperation.set(
      null
    );

    if (
      actionType === 'TRAVEL_BOOKING'
    ) {
      bookingControl.setValidators([
        Validators.required
      ]);
    }

    if (
      actionType === 'ONLINE_PURCHASE'
    ) {
      purchaseControl.setValidators([
        Validators.required
      ]);
    }

    if (
      actionType
        === 'SUBSCRIPTION_MANAGEMENT'
    ) {
      subscriptionControl.setValidators([
        Validators.required
      ]);
    }

    bookingControl.updateValueAndValidity({
      emitEvent: false
    });

    purchaseControl.updateValueAndValidity({
      emitEvent: false
    });

    subscriptionControl.updateValueAndValidity({
      emitEvent: false
    });

    cancellationControl.updateValueAndValidity({
      emitEvent: false
    });
  }

  private configureCancellationControl(
    operation:
      SubscriptionOperation | null
  ): void {

    const control =
      this.actionForm.controls
        .cancellationCompleted;

    control.clearValidators();

    if (operation === 'CANCEL') {

      control.setValidators([
        Validators.required
      ]);

      if (control.value === null) {
        control.setValue(
          false,
          {
            emitEvent: false
          }
        );
      }
    } else {

      control.setValue(
        null,
        {
          emitEvent: false
        }
      );
    }

    control.updateValueAndValidity({
      emitEvent: false
    });
  }

  private buildRequest(
    twinId: number,
    actionType: ActionType
  ) {

    const value =
      this.actionForm.getRawValue();

    return {
      twinId,

      actionType,

      transactionReference:
        value.transactionReference
          ?.trim()
        ?? '',

      description:
        value.description
          ?.trim()
        ?? '',

      actionAmount:
        value.actionAmount === null
        || value.actionAmount === undefined
          ? null
          : Number(
              value.actionAmount
            ),

      approvalProvided:
        value.approvalProvided
        ?? false,

      bookingOutcome:
        actionType === 'TRAVEL_BOOKING'
          ? value.bookingOutcome
          : null,

      purchaseOutcome:
        actionType === 'ONLINE_PURCHASE'
          ? value.purchaseOutcome
          : null,

      subscriptionOperation:
        actionType
          === 'SUBSCRIPTION_MANAGEMENT'
          ? value.subscriptionOperation
          : null,

      cancellationCompleted:
        actionType
          === 'SUBSCRIPTION_MANAGEMENT'
        && value.subscriptionOperation
          === 'CANCEL'
          ? value.cancellationCompleted
          : null,

      occurredAt:
        this.toBackendDateTime(
          value.occurredAt
        )
    };
  }

  private toBackendDateTime(
    value: string | null | undefined
  ): string | null {

    if (
      value === null
      || value === undefined
      || value.trim().length === 0
    ) {
      return null;
    }

    return value.length === 16
      ? `${value}:00`
      : value;
  }
}