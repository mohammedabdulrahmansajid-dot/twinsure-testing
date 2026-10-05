// Defines Customer policy-application events for loading, creating,
// accepting, declining, resubmitting, and clearing application state.

import { createActionGroup, emptyProps, props } from '@ngrx/store';

import { PolicyApplicationDetails } from '../models/policy-application-details.model';
import { PolicyApplicationResponse } from '../models/policy-application-response.model';
import { PolicyResponse } from '../models/policy-response.model';

export const PolicyApplicationActions = createActionGroup({
  source: 'Policy Applications',

  events: {
    'Load My Applications': emptyProps(),

    'Load My Applications Success': props<{
      applications: PolicyApplicationResponse[];
    }>(),

    'Load My Applications Failure': props<{
      error: string;
    }>(),

    'Load Application Details': props<{
      applicationId: number;
    }>(),

    'Load Application Details Success': props<{
      details: PolicyApplicationDetails;
    }>(),

    'Load Application Details Failure': props<{
      error: string;
    }>(),

    'Create Application': props<{
      twinId: number;
      productId: number;
    }>(),

    'Create Application Success': props<{
      application: PolicyApplicationResponse;
    }>(),

    'Create Application Failure': props<{
      error: string;
    }>(),

    'Accept Application': props<{
      applicationId: number;
    }>(),

    'Accept Application Success': props<{
      policy: PolicyResponse;
    }>(),

    'Accept Application Failure': props<{
      error: string;
    }>(),

    'Decline Application': props<{
      applicationId: number;
    }>(),

    'Decline Application Success': props<{
      application: PolicyApplicationResponse;
    }>(),

    'Decline Application Failure': props<{
      error: string;
    }>(),

    'Resubmit Application': props<{
      applicationId: number;
    }>(),

    'Resubmit Application Success': props<{
      application: PolicyApplicationResponse;
    }>(),

    'Resubmit Application Failure': props<{
      error: string;
    }>(),

    'Clear Selected Application': emptyProps(),

    'Clear Policy Application State': emptyProps(),

    'Clear Error': emptyProps(),
  },
});

