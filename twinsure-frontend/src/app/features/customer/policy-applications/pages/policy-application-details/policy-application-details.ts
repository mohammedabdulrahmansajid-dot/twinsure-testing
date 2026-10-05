// Displays one Customer-owned policy application and its result.
// Customers can accept or decline approved proposals and resubmit
// applications after completing requested AI Twin changes.

import {
  CurrencyPipe,
  DatePipe
} from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnDestroy,
  OnInit,
  signal
} from '@angular/core';
import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';

import {
  Store
} from '@ngrx/store';

import {
  PolicyApplicationActions
} from '../../state/policy-application.actions';
import {
  selectError,
  selectLoading,
  selectSaving,
  selectSelectedDetails
} from '../../state/policy-application.selectors';

@Component({
  selector: 'app-policy-application-details',
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink
  ],
  templateUrl:
    './policy-application-details.html',
  styleUrl:
    './policy-application-details.css',
  changeDetection:
    ChangeDetectionStrategy.OnPush
})
export class PolicyApplicationDetails
  implements OnInit, OnDestroy {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly store =
    inject(Store);

  private readonly applicationId =
    Number(
      this.route.snapshot.paramMap.get(
        'applicationId'
      )
    );

  readonly details =
    this.store.selectSignal(
      selectSelectedDetails
    );

  readonly loading =
    this.store.selectSignal(
      selectLoading
    );

  readonly saving =
    this.store.selectSignal(
      selectSaving
    );

  readonly error =
    this.store.selectSignal(
      selectError
    );

  readonly declineConfirmationVisible =
    signal(false);

  readonly application =
    computed(() =>
      this.details()?.application
      ?? null
    );

  readonly product =
    computed(() =>
      this.details()?.product
      ?? null
    );

  ngOnInit(): void {

    if (
      !Number.isInteger(
        this.applicationId
      )
      || this.applicationId <= 0
    ) {
      this.router.navigateByUrl(
        '/customer/policy-applications'
      );

      return;
    }

    this.store.dispatch(
      PolicyApplicationActions
        .loadApplicationDetails({
          applicationId:
            this.applicationId
        })
    );
  }

  ngOnDestroy(): void {

    this.store.dispatch(
      PolicyApplicationActions
        .clearSelectedApplication()
    );
  }

  acceptProposal(): void {

    if (
      this.application()?.status
        !== 'APPROVED'
    ) {
      return;
    }

    this.store.dispatch(
      PolicyApplicationActions
        .acceptApplication({
          applicationId:
            this.applicationId
        })
    );
  }

  showDeclineConfirmation(): void {

    if (
      this.application()?.status
        !== 'APPROVED'
    ) {
      return;
    }

    this.declineConfirmationVisible.set(
      true
    );
  }

  hideDeclineConfirmation(): void {

    this.declineConfirmationVisible.set(
      false
    );

    this.store.dispatch(
      PolicyApplicationActions
        .clearError()
    );
  }

  declineProposal(): void {

    if (
      this.application()?.status
        !== 'APPROVED'
    ) {
      return;
    }

    this.store.dispatch(
      PolicyApplicationActions
        .declineApplication({
          applicationId:
            this.applicationId
        })
    );

    this.declineConfirmationVisible.set(
      false
    );
  }

  resubmitApplication(): void {

    const application =
      this.application();

    if (
      application === null
      || application.status
        !== 'CHANGES_REQUIRED'
    ) {
      return;
    }

    this.store.dispatch(
      PolicyApplicationActions
        .resubmitApplication({
          applicationId:
            application.applicationId
        })
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
}