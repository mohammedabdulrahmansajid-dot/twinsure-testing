# TwinSure Demo Verification Checklist

- [x] **Public Landing Page:** Visit `http://localhost:4200/`. Verify visible "Sign in" and "Create account" CTAs.
- [x] **Customer Registration:** Click "Create account", complete registration form, verify customer creation.
- [x] **Customer Login:** Authenticate as Customer, verify HttpOnly cookie setting and redirection to `/customer/dashboard`.
- [x] **AI Twin Registration:** Register AI Twin ("Nova", "SUPERVISED", transaction limit $10,000).
- [x] **Insurance Application:** Select "TwinSure AI Protection" product and submit policy application.
- [x] **Underwriter Review:** Log in as Underwriter (`underwriter`), view pending application, verify automated risk calculation ($3,750 premium), record approval.
- [x] **Policy Issuance:** Verify policy status changes to `ACTIVE` and AI Twin configuration lock is enabled.
- [x] **AI Action Simulation:** Simulate action ($5,000 online purchase), verify compliance log.
- [x] **Incident & Claim Filing:** Report policy incident, file claim ($2,500), upload evidence URL.
- [x] **Adjuster Adjudication:** Log in as Claims Adjuster (`adjuster`), review claim, enter decision ($2,500 approved), verify notification dispatch.
- [x] **Notifications:** Log in as Customer, verify read/unread notification feed.
