// Defines queue, claim-details, review-context, and workflow actions.
// Every successful mutation triggers an authoritative details reload.

import {
  createActionGroup,
  emptyProps,
  props
} from '@ngrx/store';

import {
  ClaimDetails
} from '../models/claim-details.model';
import {
  ClaimResponse
} from '../models/claim-response.model';
import {
  ClaimReviewContext
} from '../models/claim-review-context.model';
import {
  ClaimApprovalRequest,
  ClaimReasonRequest
} from '../models/claim-workflow-request.model';

export const ClaimsAdjusterActions =
  createActionGroup({
    source: 'Claims Adjuster',

    events: {
      'Load Assigned Claims':
        emptyProps(),

      'Load Assigned Claims Success':
        props<{
          claims: ClaimResponse[];
        }>(),

      'Load Assigned Claims Failure':
        props<{
          error: string;
        }>(),

      'Load Claim Details':
        props<{
          claimId: number;
        }>(),

      'Load Claim Details Success':
        props<{
          details: ClaimDetails;
        }>(),

      'Load Claim Details Failure':
        props<{
          error: string;
        }>(),

      'Load Review Context':
        props<{
          claimId: number;
        }>(),

      'Load Review Context Success':
        props<{
          reviewContext: ClaimReviewContext;
        }>(),

      'Load Review Context Failure':
        props<{
          error: string;
        }>(),

      'Start Review':
        props<{
          claimId: number;
          request: ClaimReasonRequest;
        }>(),

      'Request Information':
        props<{
          claimId: number;
          request: ClaimReasonRequest;
        }>(),

      'Approve Claim':
        props<{
          claimId: number;
          request: ClaimApprovalRequest;
        }>(),

      'Partially Approve Claim':
        props<{
          claimId: number;
          request: ClaimApprovalRequest;
        }>(),

      'Reject Claim':
        props<{
          claimId: number;
          request: ClaimReasonRequest;
        }>(),

      'Close Claim':
        props<{
          claimId: number;
          request: ClaimReasonRequest;
        }>(),

      'Workflow Operation Success':
        props<{
          claimId: number;
        }>(),

      'Workflow Operation Failure':
        props<{
          error: string;
        }>(),

      'Clear Selected Claim':
        emptyProps(),

      'Clear Claims Adjuster State':
        emptyProps(),

      'Clear Error':
        emptyProps()
    }
  });