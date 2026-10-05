-- Target Service: ai-action-service
-- Database Engine: H2 (R2DBC Reactive)
-- Status: READ-ONLY (Safe for Production & Staging)
-- Assumptions: Schema contains 'ai_actions' and 'action_violations' tables.
-- Execution: Execute via H2 Console or R2DBC client.

-- 1. Count Action Logs by Evaluation Result
SELECT compliance_status, COUNT(*) AS count
FROM ai_actions
GROUP BY compliance_status;

-- 2. Find Violations Flagged for Claim Creation
SELECT v.violation_id, a.action_id, v.violation_type, a.customer_id
FROM action_violations v
JOIN ai_actions a ON v.action_id = a.action_id
WHERE v.is_claim_eligible = TRUE;
