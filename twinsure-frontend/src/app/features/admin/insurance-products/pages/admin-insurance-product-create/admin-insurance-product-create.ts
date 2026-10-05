// Creates a new insurance product in Draft status.
// Insurance Policy Service validates product-code uniqueness
// and persists the financial terms supplied by the Administrator.

import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  Router,
  RouterLink,
} from '@angular/router';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { AdminInsuranceProductApiService } from '../../services/admin-insurance-product-api.service';

type ProductControlName =
  | 'productCode'
  | 'productName'
  | 'description'
  | 'basePremium'
  | 'coverageLimit'
  | 'deductible';

@Component({
  selector: 'app-admin-insurance-product-create',
  imports: [
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './admin-insurance-product-create.html',
  styleUrl: './admin-insurance-product-create.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminInsuranceProductCreate {
  private readonly formBuilder =
    inject(FormBuilder);

  private readonly productApi =
    inject(AdminInsuranceProductApiService);

  private readonly router =
    inject(Router);

  readonly submitting =
    signal(false);

  readonly error =
    signal<string | null>(null);

  readonly productForm =
    this.formBuilder.nonNullable.group({
      productCode: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(50),
        ],
      ],

      productName: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(150),
        ],
      ],

      description: [
        '',
        [
          Validators.required,
          Validators.minLength(10),
          Validators.maxLength(500),
        ],
      ],

      basePremium: [
        0,
        [
          Validators.required,
          Validators.min(0.01),
        ],
      ],

      coverageLimit: [
        0,
        [
          Validators.required,
          Validators.min(0.01),
        ],
      ],

      deductible: [
        0,
        [
          Validators.required,
          Validators.min(0),
        ],
      ],
    });

  controlInvalid(
    controlName: ProductControlName,
  ): boolean {
    const control =
      this.productForm.controls[
        controlName
      ];

    return control.invalid
      && (
        control.touched
        || control.dirty
      );
  }

  descriptionLength(): number {
    return this.productForm.controls
      .description.value.length;
  }

  deductibleExceedsCoverage(): boolean {
    const coverageLimit =
      Number(
        this.productForm.controls
          .coverageLimit.value,
      );

    const deductible =
      Number(
        this.productForm.controls
          .deductible.value,
      );

    return Number.isFinite(coverageLimit)
      && Number.isFinite(deductible)
      && coverageLimit > 0
      && deductible > coverageLimit;
  }

  submit(): void {
    this.error.set(null);

    if (
      this.productForm.invalid
      || this.deductibleExceedsCoverage()
    ) {
      this.productForm.markAllAsTouched();

      if (this.deductibleExceedsCoverage()) {
        this.error.set(
          'Deductible cannot exceed the product coverage limit.',
        );
      }

      return;
    }

    const value =
      this.productForm.getRawValue();

    const productCode =
      value.productCode
        .trim()
        .toUpperCase();

    const productName =
      value.productName.trim();

    const description =
      value.description.trim();

    if (
      productCode.length < 3
      || productName.length < 3
      || description.length < 10
    ) {
      this.error.set(
        'Complete all product fields using the required minimum lengths.',
      );

      return;
    }

    this.submitting.set(true);

    this.productApi
      .createProduct({
        productCode,
        productName,
        description,
        basePremium:
          Number(value.basePremium),
        coverageLimit:
          Number(value.coverageLimit),
        deductible:
          Number(value.deductible),
      })
      .pipe(
        finalize(() =>
          this.submitting.set(false),
        ),
      )
      .subscribe({
        next: (createdProduct) => {
          this.router.navigate([
            '/admin/insurance-products',
            createdProduct.productId,
          ]);
        },

        error: (error: unknown) => {
          this.error.set(
            getApiErrorMessage(error),
          );
        },
      });
  }
}