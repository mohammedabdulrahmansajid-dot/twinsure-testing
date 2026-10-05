-- Target Service: insurance-policy-service
-- Database Engine: H2 (R2DBC Reactive)
-- Status: READ-ONLY (Safe for Production & Staging)
-- Assumptions: Schema contains 'insurance_products', 'policy_applications', and 'policies' tables.
-- Execution: Execute via H2 Console or R2DBC client.

-- 1. Check Unique Product Codes
SELECT product_code, COUNT(*) AS dupes
FROM insurance_products
GROUP BY product_code
HAVING COUNT(*) > 1;

-- 2. Validate Application Status Workflow States
SELECT status, COUNT(*) AS count
FROM policy_applications
GROUP BY status;

-- 3. Check Policy Applications without Underwriting Snapshot
SELECT application_id, status, risk_score
FROM policy_applications
WHERE status = 'APPROVED' AND (risk_score IS NULL OR calculated_premium IS NULL);

-- 4. Check Policies Issued without Matching Approved Application
SELECT p.policy_id, p.application_id, p.status
FROM policies p
LEFT JOIN policy_applications a ON p.application_id = a.application_id
WHERE a.application_id IS NULL OR a.status != 'APPROVED';
