// Validates the relationship between an AI Twin's financial controls.
// The approval threshold cannot exceed the maximum transaction limit.

import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const financialControlsValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const transactionLimit = Number(control.get('transactionLimit')?.value);

  const approvalThreshold = Number(control.get('approvalThreshold')?.value);

  if (!Number.isFinite(transactionLimit) || !Number.isFinite(approvalThreshold)) {
    return null;
  }

  return approvalThreshold <= transactionLimit
    ? null
    : {
        thresholdExceedsLimit: true,
      };
};
