// Provides the complete Underwriter review workspace for one application.
// The page refreshes authoritative application data after assessment
// and scrolls newly opened decision forms into view.

import { CurrencyPipe, DatePipe } from '@angular/common';
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

import { finalize, map, switchMap } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { PolicyApplicationDetails } from '../../../../customer/policy-applications/models/policy-application-details.model';
import { PolicyApplicationResponse } from '../../../../customer/policy-applications/models/policy-application-response.model';
import { AiTwinRiskProfile } from '../../models/ai-twin-risk-profile.model';
import { UnderwriterApplicationApiService } from '../../services/underwriter-application-api.service';

type DecisionPanel = 'ASSESS' | 'APPROVE' | 'CHANGES' | 'REJECT' | null;

@Component({
  selector: 'app-underwriter-application-details',
  imports: [CurrencyPipe,  ReactiveFormsModule, RouterLink],
  templateUrl: './underwriter-application-details.html',
  styleUrl: './underwriter-application-details.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnderwriterApplicationDetails implements OnInit {
  private readonly formBuilder = inject(FormBuilder);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly applicationApi = inject(UnderwriterApplicationApiService);

  private readonly applicationId = Number(this.route.snapshot.paramMap.get('applicationId'));

  readonly details = signal<PolicyApplicationDetails | null>(null);

  readonly riskProfile = signal<AiTwinRiskProfile | null>(null);

  readonly loading = signal(true);

  readonly saving = signal(false);

  readonly error = signal<string | null>(null);

  readonly successMessage = signal<string | null>(null);

  readonly activePanel = signal<DecisionPanel>(null);

  readonly application = computed(() => this.details()?.application ?? null);

  readonly product = computed(() => this.details()?.product ?? null);

  readonly canAssess = computed(() => this.application()?.status === 'PENDING_REVIEW');

  readonly hasCompleteAssessment = computed(() => {
    const application = this.application();

    return (
      application !== null &&
      application.riskScore !== null &&
      application.riskScore !== undefined &&
      application.riskLevel !== null &&
      application.riskLevel !== undefined &&
      application.systemRecommendation !== null &&
      application.systemRecommendation !== undefined
    );
  });

  readonly canDecide = computed(
    () => this.application()?.status === 'PENDING_REVIEW' && this.hasCompleteAssessment(),
  );

  readonly assessmentForm = this.formBuilder.nonNullable.group({
    previousIncidentCount: [0, [Validators.required, Validators.min(0), Validators.max(100)]],

    assessmentRemarks: ['', [Validators.maxLength(500)]],
  });

  readonly approvalForm = this.formBuilder.nonNullable.group({
    proposedPremium: [0, [Validators.required, Validators.min(0.01)]],

    proposedCoverageLimit: [0, [Validators.required, Validators.min(0.01)]],

    proposedDeductible: [0, [Validators.required, Validators.min(0)]],

    reason: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
  });

  readonly changesForm = this.formBuilder.nonNullable.group({
    reason: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
  });

  readonly rejectionForm = this.formBuilder.nonNullable.group({
    reason: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    if (!Number.isInteger(this.applicationId) || this.applicationId <= 0) {
      this.router.navigateByUrl('/underwriter/applications/pending');

      return;
    }

    this.loadReview();
  }

  openPanel(panel: Exclude<DecisionPanel, null>): void {
    this.error.set(null);
    this.successMessage.set(null);

    if (panel === 'APPROVE') {
      this.populateApprovalForm();
    }

    this.activePanel.set(panel);

    this.scrollToElement('underwriter-decision-panel');
  }

  closePanel(): void {
    this.activePanel.set(null);
    this.error.set(null);
  }

  runAssessment(): void {
    this.error.set(null);
    this.successMessage.set(null);

    if (this.assessmentForm.invalid) {
      this.assessmentForm.markAllAsTouched();

      return;
    }

    const value = this.assessmentForm.getRawValue();

    this.saving.set(true);

    this.applicationApi
      .assessApplication(this.applicationId, {
        previousIncidentCount: Number(value.previousIncidentCount),

        assessmentRemarks: value.assessmentRemarks.trim() || null,
      })
      .pipe(
        switchMap(() => this.applicationApi.getApplicationDetails(this.applicationId)),

        finalize(() => this.saving.set(false)),
      )
      .subscribe({
        next: (details) => {
          this.details.set(details);
          this.activePanel.set(null);

          this.successMessage.set('Risk assessment completed successfully.');

          this.populateApprovalForm();

          this.scrollToElement('underwriter-assessment-result');
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));

          this.scrollToElement('underwriter-page-message');
        },
      });
  }

  approveApplication(): void {
    this.error.set(null);
    this.successMessage.set(null);

    if (this.approvalForm.invalid) {
      this.approvalForm.markAllAsTouched();

      return;
    }

    const value = this.approvalForm.getRawValue();

    this.saving.set(true);

    this.applicationApi
      .approveApplication(this.applicationId, {
        proposedPremium: Number(value.proposedPremium),

        proposedCoverageLimit: Number(value.proposedCoverageLimit),

        proposedDeductible: Number(value.proposedDeductible),

        reason: value.reason.trim(),
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (application) => {
          this.updateApplication(application);

          this.activePanel.set(null);

          this.successMessage.set('Application approved and proposal created.');

          this.scrollToElement('underwriter-page-message');
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));

          this.scrollToElement('underwriter-page-message');
        },
      });
  }

  requestChanges(): void {
    this.error.set(null);
    this.successMessage.set(null);

    if (this.changesForm.invalid) {
      this.changesForm.markAllAsTouched();

      return;
    }

    const reason = this.changesForm.controls.reason.value.trim();

    this.saving.set(true);

    this.applicationApi
      .requestChanges(this.applicationId, {
        reason,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (application) => {
          this.updateApplication(application);

          this.activePanel.set(null);

          this.successMessage.set('Changes requested successfully.');

          this.scrollToElement('underwriter-page-message');
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));

          this.scrollToElement('underwriter-page-message');
        },
      });
  }

  rejectApplication(): void {
    this.error.set(null);
    this.successMessage.set(null);

    if (this.rejectionForm.invalid) {
      this.rejectionForm.markAllAsTouched();

      return;
    }

    const reason = this.rejectionForm.controls.reason.value.trim();

    this.saving.set(true);

    this.applicationApi
      .rejectApplication(this.applicationId, {
        reason,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (application) => {
          this.updateApplication(application);

          this.activePanel.set(null);

          this.successMessage.set('Application rejected successfully.');

          this.scrollToElement('underwriter-page-message');
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));

          this.scrollToElement('underwriter-page-message');
        },
      });
  }

  formatLabel(value: string | null | undefined): string {
    if (value === null || value === undefined || value.trim().length === 0) {
      return 'Not available';
    }

    return value
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private loadReview(): void {
    this.loading.set(true);
    this.error.set(null);
    this.successMessage.set(null);

    this.applicationApi
      .getApplicationDetails(this.applicationId)
      .pipe(
        switchMap((details) =>
          this.applicationApi.getRiskProfile(details.application.twinId).pipe(
            map((riskProfile) => ({
              details,
              riskProfile,
            })),
          ),
        ),

        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (result) => {
          this.details.set(result.details);

          this.riskProfile.set(result.riskProfile);

          this.populateApprovalForm();
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  private populateApprovalForm(): void {
    const application = this.application();

    const product = this.product();

    if (product === null) {
      return;
    }

    this.approvalForm.reset({
      proposedPremium: application?.proposedPremium ?? product.basePremium,

      proposedCoverageLimit: application?.proposedCoverageLimit ?? product.coverageLimit,

      proposedDeductible: application?.proposedDeductible ?? product.deductible,

      reason: application?.decisionReason ?? '',
    });
  }

  private updateApplication(application: PolicyApplicationResponse): void {
    const currentDetails = this.details();

    if (currentDetails === null) {
      return;
    }

    this.details.set({
      ...currentDetails,
      application,
    });
  }

  private scrollToElement(elementId: string): void {
    setTimeout(() => {
      document.getElementById(elementId)?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });
    });
  }
}
