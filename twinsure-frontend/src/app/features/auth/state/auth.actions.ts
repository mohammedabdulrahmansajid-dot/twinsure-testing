// Defines authentication events used by components, effects, and reducers.
// API operations are represented by request, success, and failure actions.

import {
  createActionGroup,
  emptyProps,
  props
} from '@ngrx/store';

import {
  AuthenticatedUser
} from '../../../core/models/authenticated-user.model';
import {
  LoginRequest
} from '../models/login-request.dto';
import {
  RegisterRequest
} from '../models/register-request.dto';
import {
  RegistrationResponse
} from '../models/registration-response.dto';

export const AuthActions =
  createActionGroup({
    source: 'Auth',
    events: {
      'Restore Session': emptyProps(),

      'Restore Session Success': props<{
        user: AuthenticatedUser;
      }>(),

      'Restore Session Empty': emptyProps(),

      'Login': props<{
        request: LoginRequest;
      }>(),

      'Login Success': props<{
        user: AuthenticatedUser;
      }>(),

      'Login Failure': props<{
        error: string;
      }>(),

      'Register': props<{
        request: RegisterRequest;
      }>(),

      'Register Success': props<{
        response: RegistrationResponse;
      }>(),

      'Register Failure': props<{
        error: string;
      }>(),

      'Logout': emptyProps(),

      'Logout Success': emptyProps(),

      
      'Clear Error': emptyProps()
    }
  });