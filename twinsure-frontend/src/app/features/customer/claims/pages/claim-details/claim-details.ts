// Displays a complete Customer claim with supporting evidence and decisions.
// Customers can add evidence metadata without uploading binary file content.

import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { ClaimDetails as ClaimDetailsModel } from '../../models/claim-details.model';
import { DocumentType } from '../../models/claim-types.model';
import { ClaimApiService } from '../../services/claim-api.service';

@Component({
  selector: 'app-claim-details',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './claim-details.html',
  styleUrl: './claim-details.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClaimDetails implements OnInit {
  private readonly formBuilder = inject(FormBuilder);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly claimApi = inject(ClaimApiService);

  private readonly claimId = Number(this.route.snapshot.paramMap.get('claimId'));

  readonly details = signal<ClaimDetailsModel | null>(null);

  readonly loading = signal(true);

  readonly saving = signal(false);

  readonly error = signal<string | null>(null);

  readonly documentTypes: DocumentType[] = [
    'INVOICE',
    'RECEIPT',
    'SCREENSHOT',
    'TRANSACTION_RECORD',
    'AI_ACTION_LOG',
    'CUSTOMER_STATEMENT',
    'OTHER',
  ];

  readonly documentForm = this.formBuilder.group({
    documentType: ['' as DocumentType | '', [Validators.required]],

    fileName: ['', [Validators.required, Validators.maxLength(255)]],

    documentReference: ['', [Validators.required, Validators.maxLength(500)]],

    description: ['', [Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    if (!Number.isInteger(this.claimId) || this.claimId <= 0) {
      this.router.navigateByUrl('/customer/claims');

      return;
    }

    this.loadClaim();
  }

  addDocument(): void {
    this.error.set(null);

    if (this.documentForm.invalid) {
      this.documentForm.markAllAsTouched();

      return;
    }

    const value = this.documentForm.getRawValue();

    if (value.documentType === null || value.documentType === '') {
      return;
    }

    this.saving.set(true);

    this.claimApi
      .addDocument(this.claimId, {
        documentType: value.documentType,

        fileName: value.fileName?.trim() ?? '',

        documentReference: value.documentReference?.trim() ?? '',

        description: value.description?.trim() || null,
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (document) => {
          const currentDetails = this.details();

          if (currentDetails !== null) {
            this.details.set({
              ...currentDetails,

              documents: [document, ...currentDetails.documents],
            });
          }

          this.documentForm.reset({
            documentType: '',
            fileName: '',
            documentReference: '',
            description: '',
          });
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

  private loadClaim(): void {
    this.loading.set(true);
    this.error.set(null);

    this.claimApi
      .getClaimDetails(this.claimId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (details) =>
          this.details.set({
            ...details,

            documents: [...details.documents].sort(
              (first, second) =>
                new Date(second.uploadedAt).getTime() - new Date(first.uploadedAt).getTime(),
            ),

            decisions: [...details.decisions].sort(
              (first, second) =>
                new Date(second.decidedAt).getTime() - new Date(first.decidedAt).getTime(),
            ),
          }),

        error: (error) => this.error.set(getApiErrorMessage(error)),
      });
  }
}
