// Defines lazy-loaded Administrator workspace routes.
// All routes inherit Admin authorization from the parent route.

import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  {
    path: 'dashboard',
    title: 'Admin Dashboard | TwinSure',
    data: {
      title: 'Administrator Dashboard',
      description: 'Monitor users, products, policies, actions, incidents, and claims.',
    },
    loadComponent: () =>
      import('../../shared/pages/role-dashboard/role-dashboard').then(
        (module) => module.RoleDashboard,
      ),
  },
  {
    path: 'users/new',
    title: 'Create Staff User | TwinSure',
    loadComponent: () =>
      import('./users/pages/admin-user-create/admin-user-create').then(
        (module) => module.AdminUserCreate,
      ),
  },
  {
    path: 'users',
    title: 'User Management | TwinSure',
    loadComponent: () =>
      import('./users/pages/admin-user-list/admin-user-list').then(
        (module) => module.AdminUserList,
      ),
  },
  {
    path: 'claims/:claimId',
    title: 'Admin Claim Details | TwinSure',
    loadComponent: () =>
      import('./claims/pages/admin-claim-details/admin-claim-details').then(
        (module) => module.AdminClaimDetails,
      ),
  },
  {
    path: 'claims',
    title: 'Claim Administration | TwinSure',
    loadComponent: () =>
      import('./claims/pages/admin-claim-list/admin-claim-list').then(
        (module) => module.AdminClaimList,
      ),
  },
  {
    path: 'policy-applications',
    title: 'Policy Applications | TwinSure',
    loadComponent: () =>
      import('./policy-applications/pages/admin-policy-application-list/admin-policy-application-list').then(
        (module) => module.AdminPolicyApplicationList,
      ),
  },
  {
    path: 'policies/:policyId',
    title: 'Policy Contract | TwinSure',
    loadComponent: () =>
      import('../customer/policies/pages/policy-details/policy-details').then(
        (module) => module.PolicyDetails,
      ),
  },
  {
    path: 'policies',
    title: 'Issued Policies | TwinSure',
    loadComponent: () =>
      import('./policies/pages/admin-policy-list/admin-policy-list').then(
        (module) => module.AdminPolicyList,
      ),
  },
  {
    path: 'customers',
    title: 'Customer Administration | TwinSure',
    loadComponent: () =>
      import('./customers/pages/admin-customer-list/admin-customer-list').then(
        (module) => module.AdminCustomerList,
      ),
  },
  {
    path: 'ai-twins',
    title: 'AI Twin Administration | TwinSure',
    loadComponent: () =>
      import('./ai-twins/pages/admin-ai-twin-list/admin-ai-twin-list').then(
        (module) => module.AdminAiTwinList,
      ),
  },
  {
    path: 'ai-actions',
    title: 'AI Action Oversight | TwinSure',
    loadComponent: () =>
      import('./ai-actions/pages/admin-ai-action-list/admin-ai-action-list').then(
        (module) => module.AdminAiActionList,
      ),
  },
  {
    path: 'incidents',
    title: 'Incident Oversight | TwinSure',
    loadComponent: () =>
      import('./incidents/pages/admin-incident-list/admin-incident-list').then(
        (module) => module.AdminIncidentList,
      ),
  },

  {
    path: 'insurance-products/new',
    title: 'Create Insurance Product | TwinSure',
    loadComponent: () =>
      import('./insurance-products/pages/admin-insurance-product-create/admin-insurance-product-create').then(
        (module) => module.AdminInsuranceProductCreate,
      ),
  },
  {
    path: 'insurance-products',
    title: 'Insurance Product Management | TwinSure',
    loadComponent: () =>
      import('./insurance-products/pages/admin-insurance-product-list/admin-insurance-product-list').then(
        (module) => module.AdminInsuranceProductList,
      ),
  },
  {
  path: 'insurance-products/:productId',
  title: 'Insurance Product Configuration | TwinSure',
  loadComponent: () =>
    import(
      './insurance-products/pages/admin-insurance-product-details/admin-insurance-product-details'
    ).then(
      (module) =>
        module.AdminInsuranceProductDetails,
    ),
},
];
