// Creates a policy application for the selected insurance product.
// The page reuses AI Twin NgRx state and dispatches the application
// submission through the Policy Application NgRx workflow.

import { CurrencyPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Store } from '@ngrx/store';

import { finalize } from 'rxjs';

import { InsuranceProductDetails } from '../../../insurance-products/models/insurance-product-details.model';

import { InsuranceProductApiService } from '../../../insurance-products/services/insurance-product-api.service';

import { AiTwinActions } from '../../../ai-twins/state/ai-twin.actions';

import {
  selectActiveAiTwins,
  selectLoading as selectAiTwinsLoading,
} from '../../../ai-twins/state/ai-twin.selectors';
import { PolicyApplicationActions } from '../../state/policy-application.actions';
import { selectError, selectSaving } from '../../state/policy-application.selectors';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';

@Component({
  selector: 'app-policy-application-create',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './policy-application-create.html',
  styleUrl: './policy-application-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PolicyApplicationCreate implements OnInit, OnDestroy {
  private readonly formBuilder = inject(FormBuilder);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly store = inject(Store);

  private readonly productApi = inject(InsuranceProductApiService);

  private readonly productId = Number(this.route.snapshot.queryParamMap.get('productId'));

  readonly product = signal<InsuranceProductDetails | null>(null);

  readonly productLoading = signal(true);

  readonly productError = signal<string | null>(null);

  readonly activeAiTwins = this.store.selectSignal(selectActiveAiTwins);

  readonly twinsLoading = this.store.selectSignal(selectAiTwinsLoading);

  readonly saving = this.store.selectSignal(selectSaving);

  readonly error = this.store.selectSignal(selectError);

  readonly applicationForm = this.formBuilder.group({
    twinId: [null as number | null, [Validators.required]],
  });

  get twinInvalid(): boolean {
    const control = this.applicationForm.controls.twinId;

    return control.invalid && (control.touched || control.dirty);
  }

  ngOnInit(): void {
    this.store.dispatch(PolicyApplicationActions.clearError());

    if (!Number.isInteger(this.productId) || this.productId <= 0) {
      this.router.navigateByUrl('/customer/insurance-products');

      return;
    }

    this.store.dispatch(AiTwinActions.loadMyAiTwins());

    this.loadProduct();
  }

  ngOnDestroy(): void {
    this.store.dispatch(PolicyApplicationActions.clearError());
  }

  submit(): void {
    this.store.dispatch(PolicyApplicationActions.clearError());

    if (this.applicationForm.invalid) {
      this.applicationForm.markAllAsTouched();

      return;
    }

    const value = this.applicationForm.getRawValue();

    if (value.twinId === null || value.twinId === undefined) {
      return;
    }

    this.store.dispatch(
      PolicyApplicationActions.createApplication({
        twinId: value.twinId,

        productId: this.productId,
      }),
    );
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private loadProduct(): void {
    this.productLoading.set(true);
    this.productError.set(null);

    this.productApi
      .getProductDetails(this.productId)
      .pipe(finalize(() => this.productLoading.set(false)))
      .subscribe({
        next: (product) => this.product.set(product),

        error: (error) => this.productError.set(getApiErrorMessage(error)),
      });
  }
}
