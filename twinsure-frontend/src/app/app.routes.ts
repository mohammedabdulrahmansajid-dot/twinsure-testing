// Defines public routes and the protected authenticated application shell.
// Every role dashboard renders inside the shared AppLayout child outlet.

import { Routes } from '@angular/router';

import { APPLICATION_ROLES } from './core/constants/application-role';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/welcome/pages/welcome/welcome').then((module) => module.Welcome),
  },
  {
    path: 'register',
    title: 'Register | TwinSure',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/pages/register/register').then((module) => module.Register),
  },
  {
    path: 'login',
    title: 'Sign In | TwinSure',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/pages/login/login').then((module) => module.Login),
  },
  {
    path: 'unauthorized',
    title: 'Access Denied | TwinSure',
    loadComponent: () =>
      import('./shared/pages/unauthorized/unauthorized').then((module) => module.Unauthorized),
  },

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layouts/app-layout/app-layout').then((module) => module.AppLayout),
    children: [
      {
        path: 'customer/dashboard',
        title: 'Customer Dashboard | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        data: {
          title: 'Customer Dashboard',
          description: 'Manage your AI Twins, insurance, actions, incidents, and claims.',
        },
        loadComponent: () =>
          import('./shared/pages/role-dashboard/role-dashboard').then(
            (module) => module.RoleDashboard,
          ),
      },
      {
        path: 'customer/profile',
        title: 'Customer Profile | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/profile/pages/customer-profile/customer-profile').then(
            (module) => module.CustomerProfile,
          ),
      },
      {
        path: 'customer/ai-twins',
        title: 'My AI Twins | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/ai-twins/pages/ai-twin-list/ai-twin-list').then(
            (module) => module.AiTwinList,
          ),
      },
      {
        path: 'customer/ai-twins/new',
        title: 'Register AI Twin | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/ai-twins/pages/ai-twin-create/ai-twin-create').then(
            (module) => module.AiTwinCreate,
          ),
      },
      {
        path: 'customer/ai-twins/:twinId',
        title: 'AI Twin Details | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/ai-twins/pages/ai-twin-details/ai-twin-details').then(
            (module) => module.AiTwinDetails,
          ),
      },
      {
        path: 'customer/insurance-products',
        title: 'Insurance Products | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/insurance-products/pages/insurance-product-list/insurance-product-list').then(
            (module) => module.InsuranceProductList,
          ),
      },
      {
        path: 'customer/insurance-products/:productId',
        title: 'Insurance Product Details | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/insurance-products/pages/insurance-product-details/insurance-product-details').then(
            (module) => module.InsuranceProductDetails,
          ),
      },
      {
        path: 'customer/policy-applications/new',
        title: 'Apply for Insurance | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/policy-applications/pages/policy-application-create/policy-application-create').then(
            (module) => module.PolicyApplicationCreate,
          ),
      },
      {
        path: 'customer/policy-applications/:applicationId',
        title: 'Policy Application Details | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/policy-applications/pages/policy-application-details/policy-application-details').then(
            (module) => module.PolicyApplicationDetails,
          ),
      },
      {
        path: 'customer/policy-applications',
        title: 'Policy Applications | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/policy-applications/pages/policy-application-list/policy-application-list').then(
            (module) => module.PolicyApplicationList,
          ),
      },
      {
        path: 'customer/policies/:policyId',
        title: 'Policy Details | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/policies/pages/policy-details/policy-details').then(
            (module) => module.PolicyDetails,
          ),
      },
      {
        path: 'customer/policies',
        title: 'My Policies | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/policies/pages/policy-list/policy-list').then(
            (module) => module.PolicyList,
          ),
      },
      {
        path: 'customer/ai-actions/simulate',
        title: 'Simulate AI Action | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/ai-actions/pages/ai-action-simulate/ai-action-simulate').then(
            (module) => module.AiActionSimulate,
          ),
      },
      {
        path: 'customer/ai-actions/:actionId',
        title: 'AI Action Evaluation | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/ai-actions/pages/ai-action-details/ai-action-details').then(
            (module) => module.AiActionDetails,
          ),
      },
      {
        path: 'customer/ai-actions',
        title: 'AI Action History | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/ai-actions/pages/ai-action-history/ai-action-history').then(
            (module) => module.AiActionHistory,
          ),
      },
      {
        path: 'customer/incidents/new',
        title: 'Report Incident | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/incidents/pages/incident-report/incident-report').then(
            (module) => module.IncidentReport,
          ),
      },
      {
        path: 'customer/incidents/:incidentId',
        title: 'Incident Details | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/incidents/pages/incident-details/incident-details').then(
            (module) => module.IncidentDetails,
          ),
      },
      {
        path: 'customer/incidents',
        title: 'My Incidents | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/incidents/pages/incident-list/incident-list').then(
            (module) => module.IncidentList,
          ),
      },

      {
        path: 'customer/claims/new',
        title: 'Create Claim | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/claims/pages/claim-create/claim-create').then(
            (module) => module.ClaimCreate,
          ),
      },
      {
        path: 'customer/claims/:claimId',
        title: 'Claim Details | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/claims/pages/claim-details/claim-details').then(
            (module) => module.ClaimDetails,
          ),
      },
      {
        path: 'customer/claims',
        title: 'My Claims | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CUSTOMER])],
        loadComponent: () =>
          import('./features/customer/claims/pages/claim-list/claim-list').then(
            (module) => module.ClaimList,
          ),
      },

      {
        path: 'underwriter/dashboard',
        title: 'Underwriter Dashboard | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.UNDERWRITER])],
        data: {
          title: 'Underwriter Dashboard',
          description: 'Review policy applications and record underwriting decisions.',
        },
        loadComponent: () =>
          import('./shared/pages/role-dashboard/role-dashboard').then(
            (module) => module.RoleDashboard,
          ),
      },
      {
        path: 'underwriter/applications/pending',
        title: 'Pending Applications | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.UNDERWRITER])],
        loadComponent: () =>
          import('./features/underwriter/policy-applications/pages/pending-application-list/pending-application-list').then(
            (module) => module.PendingApplicationList,
          ),
      },
      {
        path: 'underwriter/applications/reviewed',
        title: 'Reviewed Applications | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.UNDERWRITER])],
        loadComponent: () =>
          import('./features/underwriter/policy-applications/pages/reviewed-application-list/reviewed-application-list').then(
            (module) => module.ReviewedApplicationList,
          ),
      },
      {
        path: 'underwriter/applications/:applicationId',
        title: 'Underwriting Review | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.UNDERWRITER])],
        loadComponent: () =>
          import('./features/underwriter/policy-applications/pages/underwriter-application-details/underwriter-application-details').then(
            (module) => module.UnderwriterApplicationDetails,
          ),
      },
      {
        path: 'claims-adjuster/dashboard',
        title: 'Claims Adjuster Dashboard | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CLAIMS_ADJUSTER])],
        data: {
          title: 'Claims Adjuster Dashboard',
          description: 'Review evidence and complete assigned claim workflows.',
        },
        loadComponent: () =>
          import('./shared/pages/role-dashboard/role-dashboard').then(
            (module) => module.RoleDashboard,
          ),
      },
      {
        path: 'claims-adjuster/claims',
        title: 'Assigned Claims | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CLAIMS_ADJUSTER])],
        loadComponent: () =>
          import('./features/claims-adjuster/claims/pages/assigned-claim-list/assigned-claim-list').then(
            (module) => module.AssignedClaimList,
          ),
      },
      {
        path: 'claims-adjuster/claims/:claimId',
        title: 'Claim Review | TwinSure',
        canActivate: [roleGuard([APPLICATION_ROLES.CLAIMS_ADJUSTER])],
        loadComponent: () =>
          import('./features/claims-adjuster/claims/pages/adjuster-claim-details/adjuster-claim-details').then(
            (module) => module.AdjusterClaimDetails,
          ),
      },
      {
        path: 'admin',
        canActivate: [roleGuard([APPLICATION_ROLES.ADMIN])],
        loadChildren: () =>
          import('./features/admin/admin.routes').then((module) => module.ADMIN_ROUTES),
      },
      { 
        path: 'notifications',
        title: 'Notifications | TwinSure',
        loadComponent: () =>
          import('./features/notifications/pages/notification-list/notification-list').then(
            (module) => module.NotificationList,
          ),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
