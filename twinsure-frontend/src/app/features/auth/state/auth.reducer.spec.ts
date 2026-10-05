import { AuthActions } from './auth.actions';
import { authFeature, AuthState } from './auth.reducer';
import { APPLICATION_ROLES } from '../../../core/constants/application-role';

describe('AuthReducer', () => {
  const initialState: AuthState = {
    user: null,
    authenticated: false,
    restoring: true,
    loading: false,
    error: null,
    registrationResponse: null,
  };

  const sampleUser = {
    userId: 1,
    username: 'testcustomer',
    role: APPLICATION_ROLES.CUSTOMER,
    customerId: 10,
    status: 'ACTIVE',
  };

  it('should handle restoreSession action', () => {
    const action = AuthActions.restoreSession();
    const state = authFeature.reducer(initialState, action);

    expect(state.restoring).toBe(true);
    expect(state.error).toBeNull();
  });

  it('should handle restoreSessionSuccess action', () => {
    const action = AuthActions.restoreSessionSuccess({ user: sampleUser });
    const state = authFeature.reducer(initialState, action);

    expect(state.user).toEqual(sampleUser);
    expect(state.authenticated).toBe(true);
    expect(state.restoring).toBe(false);
  });

  it('should handle restoreSessionEmpty action', () => {
    const action = AuthActions.restoreSessionEmpty();
    const state = authFeature.reducer(initialState, action);

    expect(state.user).toBeNull();
    expect(state.authenticated).toBe(false);
    expect(state.restoring).toBe(false);
  });

  it('should handle login action', () => {
    const action = AuthActions.login({
      request: { username: 'testcustomer', password: 'password123' },
    });
    const state = authFeature.reducer(initialState, action);

    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
    expect(state.registrationResponse).toBeNull();
  });

  it('should handle loginSuccess action', () => {
    const action = AuthActions.loginSuccess({ user: sampleUser });
    const state = authFeature.reducer(initialState, action);

    expect(state.user).toEqual(sampleUser);
    expect(state.authenticated).toBe(true);
    expect(state.loading).toBe(false);
    expect(state.error).toBeNull();
  });

  it('should handle loginFailure action', () => {
    const action = AuthActions.loginFailure({ error: 'Invalid credentials' });
    const state = authFeature.reducer(initialState, action);

    expect(state.user).toBeNull();
    expect(state.authenticated).toBe(false);
    expect(state.loading).toBe(false);
    expect(state.error).toBe('Invalid credentials');
  });

  it('should handle logoutSuccess action', () => {
    const loggedInState: AuthState = {
      ...initialState,
      user: sampleUser,
      authenticated: true,
      restoring: false,
    };
    const action = AuthActions.logoutSuccess();
    const state = authFeature.reducer(loggedInState, action);

    expect(state.user).toBeNull();
    expect(state.authenticated).toBe(false);
    expect(state.restoring).toBe(false);
  });

  it('should handle clearError action', () => {
    const errorState: AuthState = { ...initialState, error: 'Some error' };
    const action = AuthActions.clearError();
    const state = authFeature.reducer(errorState, action);

    expect(state.error).toBeNull();
  });
});
