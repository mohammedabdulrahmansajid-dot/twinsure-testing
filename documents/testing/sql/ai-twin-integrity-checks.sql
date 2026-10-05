-- Target Service: ai-twin-service
-- Database Engine: H2 (R2DBC Reactive)
-- Status: READ-ONLY (Safe for Production & Staging)
-- Assumptions: Schema contains 'ai_twins' and 'ai_twin_permissions' tables.
-- Execution: Execute via H2 Console or R2DBC client.

-- 1. Check for Twins with Invalid Autonomy Level
SELECT twin_id, name, autonomy_level
FROM ai_twins
WHERE autonomy_level NOT IN ('ASSISTIVE', 'SUPERVISED', 'AUTONOMOUS');

-- 2. Find Twins with Active Policy Lock
SELECT twin_id, name, is_policy_locked
FROM ai_twins
WHERE is_policy_locked = TRUE;

-- 3. Find Orphaned Permission Records
SELECT p.permission_id, p.twin_id
FROM ai_twin_permissions p
LEFT JOIN ai_twins t ON p.twin_id = t.twin_id
WHERE t.twin_id IS NULL;
