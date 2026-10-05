// Validates that the password and confirmation fields contain
// identical values before Customer registration is submitted.

import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const passwordMatchValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const password = control.get('password')?.value;

  const confirmPassword = control.get('confirmPassword')?.value;

  if (password === null || confirmPassword === null || password === '' || confirmPassword === '') {
    return null;
  }

  return password === confirmPassword
    ? null
    : {
        passwordMismatch: true,
      };
};
