// Presents the TwinSure sign-in form and dispatches authentication actions.
// Reactive Forms provide client-side validation, while NgRx manages
// loading, backend errors, and the authenticated session.

import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  ActivatedRoute,
  RouterLink
} from '@angular/router';

import {
  Store
} from '@ngrx/store';

import {
  AuthActions
} from '../../state/auth.actions';
import {
  selectError,
  selectLoading
} from '../../state/auth.selectors';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Login {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly store =
    inject(Store);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  readonly loading =
    this.store.selectSignal(
      selectLoading
    );

  readonly error =
    this.store.selectSignal(
      selectError
    );

  readonly registered =
    signal(
      this.activatedRoute.snapshot
        .queryParamMap
        .get('registered') === 'true'
    );

  readonly loginForm =
    this.formBuilder.nonNullable.group({
      username: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(50)
        ]
      ],

      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6),
          Validators.maxLength(100)
        ]
      ]
    });

  get usernameInvalid(): boolean {

    const control =
      this.loginForm.controls.username;

    return control.invalid
      && (
        control.touched
        || control.dirty
      );
  }

  get passwordInvalid(): boolean {

    const control =
      this.loginForm.controls.password;

    return control.invalid
      && (
        control.touched
        || control.dirty
      );
  }

  submit(): void {

    this.store.dispatch(
      AuthActions.clearError()
    );

    if (this.loginForm.invalid) {

      this.loginForm.markAllAsTouched();

      return;
    }

    this.store.dispatch(
      AuthActions.login({
        request: this.loginForm.getRawValue()
      })
    );
  }
}