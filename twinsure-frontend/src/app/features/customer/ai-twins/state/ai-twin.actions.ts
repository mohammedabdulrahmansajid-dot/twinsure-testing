// Defines AI Twin loading, creation, updating, and permission events.
// The event wording uses "Ai" so NgRx generates consistent camel-case
// action creator names such as loadMyAiTwins and createAiTwin.

import {
  createActionGroup,
  emptyProps,
  props
} from '@ngrx/store';

import {
  AiTwinResponse
} from '../models/ai-twin-response.model';
import {
  CreateAiTwinRequest
} from '../models/create-ai-twin-request.model';
import {
  TwinPermissionRequest
} from '../models/twin-permission-request.model';
import {
  TwinPermissionResponse
} from '../models/twin-permission-response.model';
import {
  UpdateAiTwinRequest
} from '../models/update-ai-twin-request.model';

export const AiTwinActions =
  createActionGroup({
    source: 'AI Twins',

    events: {
      'Load My Ai Twins': emptyProps(),

      'Load My Ai Twins Success': props<{
        aiTwins: AiTwinResponse[];
      }>(),

      'Load My Ai Twins Failure': props<{
        error: string;
      }>(),

      'Load Ai Twin Details': props<{
        twinId: number;
      }>(),

      'Load Ai Twin Details Success': props<{
        aiTwin: AiTwinResponse;
      }>(),

      'Load Ai Twin Details Failure': props<{
        error: string;
      }>(),

      'Create Ai Twin': props<{
        request: CreateAiTwinRequest;
      }>(),

      'Create Ai Twin Success': props<{
        aiTwin: AiTwinResponse;
      }>(),

      'Create Ai Twin Failure': props<{
        error: string;
      }>(),

      'Update Ai Twin': props<{
        twinId: number;
        request: UpdateAiTwinRequest;
      }>(),

      'Update Ai Twin Success': props<{
        aiTwin: AiTwinResponse;
      }>(),

      'Update Ai Twin Failure': props<{
        error: string;
      }>(),

      'Load Permissions': props<{
        twinId: number;
      }>(),

      'Load Permissions Success': props<{
        permissions: TwinPermissionResponse[];
      }>(),

      'Load Permissions Failure': props<{
        error: string;
      }>(),

      'Configure Permission': props<{
        twinId: number;
        request: TwinPermissionRequest;
      }>(),

      'Configure Permission Success': props<{
        permission: TwinPermissionResponse;
      }>(),

      'Configure Permission Failure': props<{
        error: string;
      }>(),

      'Clear Selected Ai Twin': emptyProps(),

      'Clear Ai Twin State': emptyProps(),

      'Clear Error': emptyProps()
    }
  });