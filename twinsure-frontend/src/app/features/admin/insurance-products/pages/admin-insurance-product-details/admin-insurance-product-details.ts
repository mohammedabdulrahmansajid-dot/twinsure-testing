// Manages one insurance product and its complete configuration.
// Admin can update financial terms, add or update coverage rules,
// and add or update exclusions through Insurance Policy Service.

import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import {
  AdminInsuranceProductDetails as ProductDetailsModel,
  AdminProductCoverage,
  AdminProductExclusion,
  InsuranceActionType,
  ViolationType,
} from '../../models/admin-insurance-product.model';
import { AdminInsuranceProductApiService } from '../../services/admin-insurance-product-api.service';

type ProductControlName =
  'productName' | 'description' | 'basePremium' | 'coverageLimit' | 'deductible';

type CoverageControlName = 'actionType' | 'violationType' | 'coverageLimit' | 'description';

type ExclusionControlName = 'exclusionCode' | 'description';

@Component({
  selector: 'app-admin-insurance-product-details',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-insurance-product-details.html',
  styleUrl: './admin-insurance-product-details.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminInsuranceProductDetails implements OnInit {
  private readonly formBuilder = inject(FormBuilder);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);

  private readonly productApi = inject(AdminInsuranceProductApiService);

  private readonly productId = Number(this.route.snapshot.paramMap.get('productId'));

  readonly product = signal<ProductDetailsModel | null>(null);

  readonly loading = signal(true);

  readonly savingProduct = signal(false);

  readonly savingCoverage = signal(false);

  readonly savingExclusion = signal(false);

  readonly error = signal<string | null>(null);

  readonly success = signal<string | null>(null);

  readonly editingCoverage = signal<AdminProductCoverage | null>(null);

  readonly editingExclusion = signal<AdminProductExclusion | null>(null);

  readonly actionTypes: InsuranceActionType[] = [
    'TRAVEL_BOOKING',
    'ONLINE_PURCHASE',
    'SUBSCRIPTION_MANAGEMENT',
  ];

  readonly violationTypes: ViolationType[] = [
    'WRONG_ACTION',
    'APPROVAL_MISSING',
    'LIMIT_EXCEEDED',
    'DUPLICATE_ACTION',
    'MISSED_CANCELLATION',
  ];

  readonly productForm = this.formBuilder.nonNullable.group({
    productName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]],

    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],

    basePremium: [0, [Validators.required, Validators.min(0.01)]],

    coverageLimit: [0, [Validators.required, Validators.min(0.01)]],

    deductible: [0, [Validators.required, Validators.min(0)]],
  });

  readonly coverageForm = this.formBuilder.group({
    actionType: ['' as InsuranceActionType | '', [Validators.required]],

    violationType: ['' as ViolationType | '', [Validators.required]],

    coverageLimit: [null as number | null, [Validators.min(0.01)]],

    description: ['', [Validators.required, Validators.maxLength(500)]],
  });

  readonly exclusionForm = this.formBuilder.nonNullable.group({
    exclusionCode: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],

    description: ['', [Validators.required, Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    if (!Number.isInteger(this.productId) || this.productId <= 0) {
      this.router.navigateByUrl('/admin/insurance-products');

      return;
    }

    this.loadProduct();
  }

  reload(): void {
    this.loadProduct();
  }

  productControlInvalid(controlName: ProductControlName): boolean {
    const control = this.productForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  coverageControlInvalid(controlName: CoverageControlName): boolean {
    const control = this.coverageForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  exclusionControlInvalid(controlName: ExclusionControlName): boolean {
    const control = this.exclusionForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  deductibleExceedsProductCoverage(): boolean {
    const coverageLimit = Number(this.productForm.controls.coverageLimit.value);

    const deductible = Number(this.productForm.controls.deductible.value);

    return (
      Number.isFinite(coverageLimit) &&
      Number.isFinite(deductible) &&
      coverageLimit > 0 &&
      deductible > coverageLimit
    );
  }

  coverageExceedsProductLimit(): boolean {
    const currentProduct = this.product();

    const coverageLimit = this.coverageForm.controls.coverageLimit.value;

    if (currentProduct === null || coverageLimit === null || coverageLimit === undefined) {
      return false;
    }

    return Number(coverageLimit) > Number(currentProduct.coverageLimit);
  }

  saveProduct(): void {
    this.error.set(null);
    this.success.set(null);

    if (this.productForm.invalid || this.deductibleExceedsProductCoverage()) {
      this.productForm.markAllAsTouched();

      if (this.deductibleExceedsProductCoverage()) {
        this.error.set('Deductible cannot exceed the product coverage limit.');
      }

      return;
    }

    const value = this.productForm.getRawValue();

    const productName = value.productName.trim();

    const description = value.description.trim();

    if (productName.length < 3 || description.length < 10) {
      this.error.set('Product name and description do not meet the required lengths.');

      return;
    }

    this.savingProduct.set(true);

    this.productApi
      .updateProduct(this.productId, {
        productName,
        description,
        basePremium: Number(value.basePremium),
        coverageLimit: Number(value.coverageLimit),
        deductible: Number(value.deductible),
      })
      .pipe(finalize(() => this.savingProduct.set(false)))
      .subscribe({
        next: (updatedProduct) => {
          const currentProduct = this.product();

          if (currentProduct !== null) {
            this.product.set({
              ...currentProduct,
              ...updatedProduct,
            });
          }

          this.success.set('Insurance product details were updated successfully.');
        },

        error: (error: unknown) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  startCoverageEdit(coverage: AdminProductCoverage): void {
    this.error.set(null);
    this.success.set(null);

    this.editingCoverage.set(coverage);

    this.coverageForm.reset({
      actionType: coverage.actionType,
      violationType: coverage.violationType,
      coverageLimit: coverage.coverageLimit,
      description: coverage.description,
    });
  }

  cancelCoverageEdit(): void {
    this.editingCoverage.set(null);

    this.coverageForm.reset({
      actionType: '',
      violationType: '',
      coverageLimit: null,
      description: '',
    });
  }

  saveCoverage(): void {
    this.error.set(null);
    this.success.set(null);

    if (this.coverageForm.invalid || this.coverageExceedsProductLimit()) {
      this.coverageForm.markAllAsTouched();

      if (this.coverageExceedsProductLimit()) {
        this.error.set('A coverage-rule limit cannot exceed the product coverage limit.');
      }

      return;
    }

    const value = this.coverageForm.getRawValue();

    if (
      value.actionType === null ||
      value.actionType === '' ||
      value.violationType === null ||
      value.violationType === ''
    ) {
      this.error.set('Action type and violation type are required.');

      return;
    }

    const description = value.description?.trim() ?? '';

    if (description.length === 0) {
      this.error.set('Coverage description is required.');

      return;
    }

    this.savingCoverage.set(true);
    const existingCoverage = this.editingCoverage();

    const request = {
      actionType: existingCoverage?.actionType ?? value.actionType,

      violationType: existingCoverage?.violationType ?? value.violationType,

      coverageLimit:
        value.coverageLimit === null || value.coverageLimit === undefined
          ? null
          : Number(value.coverageLimit),

      description,
    };

    const operation =
      this.editingCoverage() === null
        ? this.productApi.addCoverage(this.productId, request)
        : this.productApi.updateCoverage(this.productId, request);

    operation.pipe(finalize(() => this.savingCoverage.set(false))).subscribe({
      next: () => {
        const editing = this.editingCoverage() !== null;

        this.cancelCoverageEdit();

        this.success.set(
          editing
            ? 'Coverage rule was updated successfully.'
            : 'Coverage rule was added successfully.',
        );

        this.reloadProductData();
      },

      error: (error: unknown) => {
        this.error.set(getApiErrorMessage(error));
      },
    });
  }

  startExclusionEdit(exclusion: AdminProductExclusion): void {
    this.error.set(null);
    this.success.set(null);

    this.editingExclusion.set(exclusion);

    this.exclusionForm.reset({
      exclusionCode: exclusion.exclusionCode,
      description: exclusion.description,
    });
  }

  cancelExclusionEdit(): void {
    this.editingExclusion.set(null);

    this.exclusionForm.reset({
      exclusionCode: '',
      description: '',
    });
  }

  saveExclusion(): void {
    this.error.set(null);
    this.success.set(null);

    if (this.exclusionForm.invalid) {
      this.exclusionForm.markAllAsTouched();

      return;
    }

    const value = this.exclusionForm.getRawValue();

    const description = value.description.trim();
    const existingExclusion = this.editingExclusion();
    const exclusionCode =
      existingExclusion?.exclusionCode ?? value.exclusionCode.trim().toUpperCase();

    if (exclusionCode.length < 3 || description.length === 0) {
      this.error.set('Exclusion code and description are required.');

      return;
    }

    const request = {
      exclusionCode,
      description,
    };

    this.savingExclusion.set(true);

    const operation =
      this.editingExclusion() === null
        ? this.productApi.addExclusion(this.productId, request)
        : this.productApi.updateExclusion(this.productId, request);

    operation.pipe(finalize(() => this.savingExclusion.set(false))).subscribe({
      next: () => {
        const editing = this.editingExclusion() !== null;

        this.cancelExclusionEdit();

        this.success.set(
          editing
            ? 'Product exclusion was updated successfully.'
            : 'Product exclusion was added successfully.',
        );

        this.reloadProductData();
      },

      error: (error: unknown) => {
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

  private loadProduct(): void {
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);

    this.productApi
      .getProductDetails(this.productId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (product) => {
          this.setProduct(product);
        },

        error: (error: unknown) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  private reloadProductData(): void {
    this.productApi.getProductDetails(this.productId).subscribe({
      next: (product) => {
        this.setProduct(product);
      },

      error: (error: unknown) => {
        this.error.set(getApiErrorMessage(error));
      },
    });
  }

  private setProduct(product: ProductDetailsModel): void {
    this.product.set({
      ...product,

      coverages: [...product.coverages].sort(
        (first, second) =>
          first.actionType.localeCompare(second.actionType) ||
          first.violationType.localeCompare(second.violationType),
      ),

      exclusions: [...product.exclusions].sort((first, second) =>
        first.exclusionCode.localeCompare(second.exclusionCode),
      ),
    });

    this.productForm.reset({
      productName: product.productName,
      description: product.description,
      basePremium: Number(product.basePremium),
      coverageLimit: Number(product.coverageLimit),
      deductible: Number(product.deductible),
    });
  }
}
