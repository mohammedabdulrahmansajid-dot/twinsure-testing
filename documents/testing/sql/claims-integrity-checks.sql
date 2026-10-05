-- Target Service: claims-service
-- Database Engine: H2 (R2DBC Reactive)
-- Status: READ-ONLY (Safe for Production & Staging)
-- Assumptions: Schema contains 'incidents', 'claims', and 'claim_evidences' tables.
-- Execution: Execute via H2 Console or R2DBC client.

-- 1. Check for Duplicate Claim Filings per Incident
SELECT incident_id, COUNT(*) AS claim_count
FROM claims
GROUP BY incident_id
HAVING COUNT(*) > 1;

-- 2. Audit Adjuster Assigned Claim Counts
SELECT adjuster_id, COUNT(*) AS assigned_claims
FROM claims
WHERE adjuster_id IS NOT NULL
GROUP BY adjuster_id;

-- 3. Check Approved Claims with Approved Amount Exceeding Claimed Amount
SELECT claim_id, claimed_amount, approved_amount
FROM claims
WHERE status = 'APPROVED' AND approved_amount > claimed_amount;
