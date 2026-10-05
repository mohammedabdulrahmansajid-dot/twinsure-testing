// Registers application-wide Angular, HTTP, Router, and NgRx providers.
// Authentication and feature states are global because guards, layouts,
// and every role area require access to the authenticated user session.

import {
  ApplicationConfig,
  isDevMode,
  provideBrowserGlobalErrorListeners
} from '@angular/core';
import {
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {
  provideRouter
} from '@angular/router';

import {
  provideEffects
} from '@ngrx/effects';
import {
  provideRouterStore
} from '@ngrx/router-store';
import {
  provideState,
  provideStore
} from '@ngrx/store';
import {
  provideStoreDevtools
} from '@ngrx/store-devtools';

import {
  routes
} from './app.routes';
import {
  credentialsInterceptor
} from './core/http/credentials.interceptor';

import {
  AuthEffects
} from './features/auth/state/auth.effects';
import {
  authFeature
} from './features/auth/state/auth.reducer';

import {
  NotificationEffects
} from './features/notifications/state/notification.effects';
import {
  notificationFeature
} from './features/notifications/state/notification.reducer';

import {
  AiTwinEffects
} from './features/customer/ai-twins/state/ai-twin.effects';
import {
  aiTwinFeature
} from './features/customer/ai-twins/state/ai-twin.reducer';

import {
  PolicyApplicationEffects
} from './features/customer/policy-applications/state/policy-application.effects';
import {
  policyApplicationFeature
} from './features/customer/policy-applications/state/policy-application.reducer';

import {
  ClaimsAdjusterEffects
} from './features/claims-adjuster/claims/state/claims-adjuster.effects';
import {
  claimsAdjusterFeature
} from './features/claims-adjuster/claims/state/claims-adjuster.reducer';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),

    provideRouter(
      routes
    ),

    provideHttpClient(
      withInterceptors([
        credentialsInterceptor
      ])
    ),

    provideStore(),

    provideState(
      authFeature
    ),

    provideState(
      notificationFeature
    ),

    provideState(
      aiTwinFeature
    ),

    provideState(
      policyApplicationFeature
    ),

    provideState(
      claimsAdjusterFeature
    ),

    provideEffects(
      AuthEffects,
      NotificationEffects,
      AiTwinEffects,
      PolicyApplicationEffects,
      ClaimsAdjusterEffects
    ),

    provideRouterStore(),

    provideStoreDevtools({
      maxAge: 25,
      logOnly: !isDevMode(),
      autoPause: true,
      trace: false,
      connectInZone: false
    })
  ]
};