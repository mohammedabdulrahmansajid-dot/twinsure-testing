// Displays the complete assigned-claim review workspace.
// Action forms are state-aware, use reactive validation,
// and scroll into view when the Adjuster selects an operation.

import { CurrencyPipe, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Store } from '@ngrx/store';

import { ClaimActionPanel, ClaimStatus } from '../../models/claim-types.model';
import { ClaimsAdjusterActions } from '../../state/claims-adjuster.actions';
import {
  selectContextError,
  selectContextLoading,
  selectError,
  selectLoading,
  selectReviewContext,
  selectSaving,
  selectSelectedDetails,
} from '../../state/claims-adjuster.selectors';

@Component({
  selector: 'app-adjuster-claim-details',

  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink],

  templateUrl: './adjuster-claim-details.html',

  styleUrl: './adjuster-claim-details.css',

  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdjusterClaimDetails implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly store = inject(Store);

  private readonly formBuilder = inject(FormBuilder);

  private readonly claimId = Number(this.route.snapshot.paramMap.get('claimId'));

  @ViewChild('actionPanel')
  private actionPanel: ElementRef<HTMLElement> | undefined;

  readonly details = this.store.selectSignal(selectSelectedDetails);

  readonly reviewContext = this.store.selectSignal(selectReviewContext);

  readonly loading = this.store.selectSignal(selectLoading);

  readonly contextLoading = this.store.selectSignal(selectContextLoading);

  readonly saving = this.store.selectSignal(selectSaving);

  readonly error = this.store.selectSignal(selectError);

  readonly contextError = this.store.selectSignal(selectContextError);

  readonly activePanel = signal<ClaimActionPanel>(null);

  readonly claim = computed(() => this.details()?.claim ?? null);

  readonly incident = computed(() => this.details()?.incident ?? null);

  readonly documents = computed(() => this.details()?.documents ?? []);

  readonly decisions = computed(() => this.details()?.decisions ?? []);
  readonly latestInformationRequest = computed(
    () =>
      this.decisions().find((decision) => decision.decisionType === 'REQUEST_INFORMATION') ?? null,
  );

  readonly latestCustomerDocument = computed(() => this.documents()[0] ?? null);

  readonly customerResponseReceived = computed(() => {
    if (this.claim()?.status !== 'MORE_INFORMATION_REQUIRED') {
      return false;
    }

    const informationRequest = this.latestInformationRequest();

    const customerDocument = this.latestCustomerDocument();

    if (informationRequest === null || customerDocument === null) {
      return false;
    }

    return (
      new Date(customerDocument.uploadedAt).getTime() >
      new Date(informationRequest.decidedAt).getTime()
    );
  });

  readonly reasonForm = this.formBuilder.nonNullable.group({
    reason: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
  });

  readonly approvalForm = this.formBuilder.nonNullable.group({
    approvedAmount: [0, [Validators.required, Validators.min(0.01)]],

    reason: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    if (!Number.isInteger(this.claimId) || this.claimId <= 0) {
      this.router.navigateByUrl('/claims-adjuster/claims');

      return;
    }

    this.reloadWorkspace();
  }

  ngOnDestroy(): void {
    this.store.dispatch(ClaimsAdjusterActions.clearSelectedClaim());
  }

  reloadWorkspace(): void {
    this.store.dispatch(
      ClaimsAdjusterActions.loadClaimDetails({
        claimId: this.claimId,
      }),
    );

    this.store.dispatch(
      ClaimsAdjusterActions.loadReviewContext({
        claimId: this.claimId,
      }),
    );
  }

  openPanel(panel: ClaimActionPanel): void {
    if (panel === null) {
      return;
    }

    this.store.dispatch(ClaimsAdjusterActions.clearError());

    this.activePanel.set(panel);

    this.reasonForm.reset({
      reason: '',
    });

    const maximumPayable = this.reviewContext()?.maximumPayableAmount ?? 0;

    this.approvalForm.reset({
      approvedAmount: panel === 'APPROVE' ? maximumPayable : 0,
      reason: '',
    });

    setTimeout(() => {
      this.actionPanel?.nativeElement.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });

      const firstInput = this.actionPanel?.nativeElement.querySelector<
        HTMLInputElement | HTMLTextAreaElement
      >('input:not([readonly]), textarea');

      firstInput?.focus();
    });
  }

  closePanel(): void {
    if (this.saving()) {
      return;
    }

    this.activePanel.set(null);

    this.store.dispatch(ClaimsAdjusterActions.clearError());
  }

  submitReasonOperation(): void {
    if (this.reasonForm.invalid) {
      this.reasonForm.markAllAsTouched();
      return;
    }

    const panel = this.activePanel();

    const request = {
      reason: this.reasonForm.getRawValue().reason.trim(),
    };

    switch (panel) {
      case 'START_REVIEW':
        this.store.dispatch(
          ClaimsAdjusterActions.startReview({
            claimId: this.claimId,
            request,
          }),
        );
        break;

      case 'REQUEST_INFORMATION':
        this.store.dispatch(
          ClaimsAdjusterActions.requestInformation({
            claimId: this.claimId,
            request,
          }),
        );
        break;

      case 'REJECT':
        this.store.dispatch(
          ClaimsAdjusterActions.rejectClaim({
            claimId: this.claimId,
            request,
          }),
        );
        break;

      case 'CLOSE':
        this.store.dispatch(
          ClaimsAdjusterActions.closeClaim({
            claimId: this.claimId,
            request,
          }),
        );
        break;

      default:
        return;
    }

    this.activePanel.set(null);
  }

  submitApproval(): void {
    if (this.approvalForm.invalid) {
      this.approvalForm.markAllAsTouched();
      return;
    }

    const panel = this.activePanel();

    const value = this.approvalForm.getRawValue();

    const request = {
      approvedAmount: Number(value.approvedAmount),
      reason: value.reason.trim(),
    };

    if (panel === 'APPROVE') {
      this.store.dispatch(
        ClaimsAdjusterActions.approveClaim({
          claimId: this.claimId,
          request,
        }),
      );

      this.activePanel.set(null);
      return;
    }

    if (panel === 'PARTIALLY_APPROVE') {
      this.store.dispatch(
        ClaimsAdjusterActions.partiallyApproveClaim({
          claimId: this.claimId,
          request,
        }),
      );

      this.activePanel.set(null);
    }
  }

  canStartReview(status: ClaimStatus): boolean {
    return status === 'ASSIGNED' || status === 'MORE_INFORMATION_REQUIRED';
  }

  canMakeDecision(status: ClaimStatus): boolean {
    return status === 'UNDER_REVIEW';
  }

  canClose(status: ClaimStatus): boolean {
    return status === 'APPROVED' || status === 'PARTIALLY_APPROVED' || status === 'REJECTED';
  }

  statusLabel(status: ClaimStatus): string {
    switch (status) {
      case 'MORE_INFORMATION_REQUIRED':
        return 'More Information Required';

      case 'PARTIALLY_APPROVED':
        return 'Partially Approved';

      default:
        return this.formatLabel(status);
    }
  }

  statusClasses(status: ClaimStatus): string {
    switch (status) {
      case 'ASSIGNED':
        return 'bg-blue-100 text-blue-700';

      case 'UNDER_REVIEW':
        return 'bg-violet-100 text-violet-700';

      case 'MORE_INFORMATION_REQUIRED':
        return 'bg-amber-100 text-amber-800';

      case 'APPROVED':
        return 'bg-emerald-100 text-emerald-700';

      case 'PARTIALLY_APPROVED':
        return 'bg-cyan-100 text-cyan-800';

      case 'REJECTED':
        return 'bg-red-100 text-red-700';

      case 'CLOSED':
        return 'bg-slate-200 text-slate-700';

      default:
        return 'bg-slate-100 text-slate-700';
    }
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }
}
