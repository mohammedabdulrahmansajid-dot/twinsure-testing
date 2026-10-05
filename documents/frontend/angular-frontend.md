# Angular Frontend Documentation (`twinsure-frontend`)

1. **Purpose:** Provides a responsive, accessible single-page web interface for Customers, Underwriters, Claims Adjusters, and System Administrators.
2. **Angular Version:** Angular 21 (`^21.2.0`) utilizing Standalone Components, Signals, and modern Control Flow (`@if`, `@for`).
3. **Folder Structure:**
   - `src/app/core/`: Guards, interceptors, constants, models, global utilities.
   - `src/app/features/`: Feature modules (`auth`, `customer`, `underwriter`, `claims-adjuster`, `admin`, `notifications`, `welcome`).
   - `src/app/layouts/`: Shared shell layout (`AppLayout`) with sidebar navigation.
   - `src/app/shared/`: Shared components, pipes, UI controls, role dashboards.
4. **Shared Layout:** `AppLayout` renders standard top header, collapsible role-based sidebar navigation, and main router child outlet `<router-outlet>`.
5. **Public Routes:** `/` (Welcome landing page), `/login` (Sign In), `/register` (Customer Account Registration), `/unauthorized` (Access Denied).
6. **Customer Routes:** `/customer/dashboard`, `/customer/profile`, `/customer/ai-twins`, `/customer/ai-twins/new`, `/customer/ai-twins/:twinId`, `/customer/insurance-products`, `/customer/policy-applications`, `/customer/policies`, `/customer/ai-actions`, `/customer/ai-actions/simulate`, `/customer/incidents`, `/customer/claims`.
7. **Underwriter Routes:** `/underwriter/dashboard`, `/underwriter/applications/pending`, `/underwriter/applications/reviewed`, `/underwriter/applications/:applicationId`.
8. **Claims Adjuster Routes:** `/claims-adjuster/dashboard`, `/claims-adjuster/claims`, `/claims-adjuster/claims/:claimId`.
9. **Admin Routes:** `/admin` (lazy loaded module containing sub-routes for users, customers, ai-twins, products, policies, applications, actions, incidents, claims).
10. **Authentication Model:** Session token is managed by `identity-service`. Frontend holds authenticated user state (`AuthenticatedUser`) in NgRx Store and signal signals.
11. **HttpOnly Cookie Behavior:** `JWT-TOKEN` cookie is managed exclusively by the browser and HTTP response headers. JavaScript code never reads or stores JWT strings in localStorage or sessionStorage.
12. **Guards:** `authGuard` (protects private shell), `guestGuard` (redirects logged-in users away from login/register), `roleGuard` (enforces role authorization).
13. **HttpClient Configuration / Interceptors:** Interceptor injects `withCredentials: true` into all outgoing requests to ensure HttpOnly cookies are attached automatically.
14. **NgRx Features:** `@ngrx/store`, `@ngrx/effects`, `@ngrx/entity`, `@ngrx/router-store`. State features include `authFeature` (`auth.actions.ts`, `auth.reducer.ts`, `auth.effects.ts`, `auth.selectors.ts`).
15. **Signal / Local State Usage:** Component UI states (loading flags, tab selection, drawer toggles, filter strings) use native Angular `signal()`, `computed()`, and `effect()`.
16. **Form Validation:** Angular Reactive Forms (`FormGroup`, `FormControl`, `Validators`). Validates required fields, email regex, min/max lengths, numeric limits, and custom match validators.
17. **API Services:** `AuthApiService`, `CustomerService`, `AiTwinService`, `InsuranceProductService`, `PolicyApplicationService`, `PolicyService`, `AiActionService`, `IncidentService`, `ClaimService`, `NotificationService`, `AdminService`.
18. **Error Handling:** Centralized HttpErrorInterceptor captures API gateway error responses and displays toast notifications or inline error messages.
19. **Styling Approach:** Tailwind CSS (v4.1.12) with custom light mode design tokens, smooth color transitions (`blue-700`, `slate-50`, `slate-900`), and accessible focus states.
20. **Testing Setup:** Vitest (`vitest` v5.0.3, `jsdom` v28.0.0) configured via `@angular/build:unit-test`.
21. **Tests Added:** Unit specs authored for Auth Reducer (`auth.reducer.spec.ts`), Auth API Service (`auth-api.service.spec.ts`), Route Guards, and root App component.
22. **Production Build:** Built via `ng build --configuration production` producing optimized bundles in `dist/twinsure-frontend/browser`.
23. **Environment Configuration:** `src/environments/environment.ts` (`apiBaseUrl: 'http://localhost:8080'`).
24. **Known Limitations:** Offline caching and PWA service workers not enabled.
