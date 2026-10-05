// Manages Customer profile viewing, creation, and updating.
// Signals hold local page state, while Reactive Forms provide
// frontend validation matching Customer Service validation rules.

import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { CustomerProfileRequest } from '../../models/customer-profile-request.model';
import { CustomerProfileResponse } from '../../models/customer-profile-response.model';
import { CustomerProfileApiService } from '../../services/customer-profile-api.service';

type ProfileControlName = 'fullName' | 'email' | 'phone' | 'address';

@Component({
  selector: 'app-customer-profile',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './customer-profile.html',
  styleUrl: './customer-profile.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerProfile implements OnInit {
  private readonly formBuilder = inject(FormBuilder);

  private readonly profileApi = inject(CustomerProfileApiService);

  readonly profile = signal<CustomerProfileResponse | null>(null);

  readonly loading = signal(true);

  readonly saving = signal(false);

  readonly editing = signal(false);

  readonly error = signal<string | null>(null);

  readonly successMessage = signal<string | null>(null);

  readonly loadingItems = [1, 2, 3, 4];

  readonly profileForm = this.formBuilder.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],

    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],

    phone: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(20)]],

    address: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  controlInvalid(controlName: ProfileControlName): boolean {
    const control = this.profileForm.controls[controlName];

    return control.invalid && (control.touched || control.dirty);
  }

  startEditing(): void {
    const currentProfile = this.profile();

    if (currentProfile === null) {
      return;
    }

    this.error.set(null);
    this.successMessage.set(null);

    this.profileForm.setValue({
      fullName: currentProfile.fullName,

      email: currentProfile.email,

      phone: currentProfile.phone,

      address: currentProfile.address,
    });

    this.editing.set(true);
  }

  cancelEditing(): void {
    this.editing.set(false);
    this.error.set(null);
    this.successMessage.set(null);

    this.resetFormFromProfile();
  }

  submit(): void {
    this.error.set(null);
    this.successMessage.set(null);

    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();

      return;
    }

    const formValue = this.profileForm.getRawValue();

    const request: CustomerProfileRequest = {
      fullName: formValue.fullName.trim(),

      email: formValue.email.trim(),

      phone: formValue.phone.trim(),

      address: formValue.address.trim(),
    };

    if (this.profile() === null) {
      this.createProfile(request);

      return;
    }

    this.updateProfile(request);
  }

  private loadProfile(): void {
    this.loading.set(true);
    this.error.set(null);

    this.profileApi
      .getProfile()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (profile) => {
          this.profile.set(profile);
          this.editing.set(false);

          this.resetFormFromProfile();
        },

        error: (error) => {
          if (error instanceof HttpErrorResponse && error.status === 404) {
            this.profile.set(null);
            this.editing.set(true);
            this.profileForm.reset();

            return;
          }

          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  private createProfile(request: CustomerProfileRequest): void {
    this.saving.set(true);

    this.profileApi
      .createProfile(request)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (profile) => {
          this.profile.set(profile);
          this.editing.set(false);

          this.successMessage.set(
            'Customer profile created successfully. ' +
              'Sign out and sign in again before using ' +
              'Customer workflows so the refreshed session ' +
              'contains your Customer ID.',
          );

          this.resetFormFromProfile();
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  private updateProfile(request: CustomerProfileRequest): void {
    this.saving.set(true);

    this.profileApi
      .updateProfile(request)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (profile) => {
          this.profile.set(profile);
          this.editing.set(false);

          this.successMessage.set('Customer profile updated successfully.');

          this.resetFormFromProfile();
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }

  private resetFormFromProfile(): void {
    const currentProfile = this.profile();

    if (currentProfile === null) {
      this.profileForm.reset();

      return;
    }

    this.profileForm.reset({
      fullName: currentProfile.fullName,

      email: currentProfile.email,

      phone: currentProfile.phone,

      address: currentProfile.address,
    });
  }
}
