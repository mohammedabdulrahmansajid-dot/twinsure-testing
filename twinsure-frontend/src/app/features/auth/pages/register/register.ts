// Presents Customer account registration with Reactive Forms.
// The form performs frontend validation and dispatches a typed
// NgRx registration action after all validation rules pass.

import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Store } from '@ngrx/store';

import { AuthActions } from '../../state/auth.actions';
import { selectError, selectLoading } from '../../state/auth.selectors';
import { passwordMatchValidator } from '../../validators/password-match.validator';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Register implements OnInit, OnDestroy {
  private readonly formBuilder = inject(FormBuilder);

  private readonly store = inject(Store);

  readonly loading = this.store.selectSignal(selectLoading);

  readonly error = this.store.selectSignal(selectError);

  readonly registerForm = this.formBuilder.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],

      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],

      confirmPassword: ['', [Validators.required]],
    },
    {
      validators: [passwordMatchValidator],
    },
  );

  get usernameInvalid(): boolean {
    const control = this.registerForm.controls.username;

    return control.invalid && (control.touched || control.dirty);
  }

  get passwordInvalid(): boolean {
    const control = this.registerForm.controls.password;

    return control.invalid && (control.touched || control.dirty);
  }

  get confirmPasswordInvalid(): boolean {
    const control = this.registerForm.controls.confirmPassword;

    const confirmationWasUsed = control.touched || control.dirty;

    return (
      confirmationWasUsed &&
      (control.hasError('required') || this.registerForm.hasError('passwordMismatch'))
    );
  }

  ngOnInit(): void {
    this.store.dispatch(AuthActions.clearError());
  }

  ngOnDestroy(): void {
    this.store.dispatch(AuthActions.clearError());
  }

  submit(): void {
    this.store.dispatch(AuthActions.clearError());

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();

      return;
    }

    const { username, password } = this.registerForm.getRawValue();

    this.store.dispatch(
      AuthActions.register({
        request: {
          username: username.trim(),
          password,
        },
      }),
    );
  }
}
