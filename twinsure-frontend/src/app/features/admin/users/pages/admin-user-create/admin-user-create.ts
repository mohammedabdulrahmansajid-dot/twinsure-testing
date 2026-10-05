// Creates Underwriter and Claims Adjuster accounts through Identity Service.
// The backend restricts staff roles and securely encodes the password.

import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { finalize } from 'rxjs';

import { getApiErrorMessage } from '../../../../../core/http/api-error.util';
import { StaffRole } from '../../models/admin-user.model';
import { AdminUserApiService } from '../../services/admin-user-api.service';

type StaffControlName = 'username' | 'password' | 'role';

interface StaffRoleOption {
  value: StaffRole;
  label: string;
  description: string;
}

@Component({
  selector: 'app-admin-user-create',

  imports: [ReactiveFormsModule, RouterLink],

  templateUrl: './admin-user-create.html',

  styleUrl: './admin-user-create.css',

  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminUserCreate {
  private readonly formBuilder = inject(FormBuilder);

  private readonly userApi = inject(AdminUserApiService);

  private readonly router = inject(Router);

  readonly submitting = signal(false);

  readonly error = signal<string | null>(null);

  readonly showPassword = signal(false);

  readonly roleOptions: StaffRoleOption[] = [
    {
      value: 'UNDERWRITER',
      label: 'Underwriter',
      description: 'Reviews policy applications and records underwriting decisions.',
    },
    {
      value: 'CLAIMS_ADJUSTER',
      label: 'Claims Adjuster',
      description: 'Reviews assigned claims, evidence, and compensation decisions.',
    },
  ];

  readonly staffForm = this.formBuilder.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],

    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],

    role: ['' as StaffRole | '', [Validators.required]],
  });

  controlInvalid(controlName: StaffControlName): boolean {
    const control = this.staffForm.controls[controlName];

    return control.invalid && (control.touched || control.dirty);
  }

  togglePasswordVisibility(): void {
    this.showPassword.update((visible) => !visible);
  }

  submit(): void {
    this.error.set(null);

    if (this.staffForm.invalid) {
      this.staffForm.markAllAsTouched();

      return;
    }

    const value = this.staffForm.getRawValue();

    if (value.role === '') {
      return;
    }

    this.submitting.set(true);

    this.userApi
      .createStaffUser({
        username: value.username.trim(),

        password: value.password,

        role: value.role,
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.router.navigateByUrl('/admin/users');
        },

        error: (error) => {
          this.error.set(getApiErrorMessage(error));
        },
      });
  }
}
