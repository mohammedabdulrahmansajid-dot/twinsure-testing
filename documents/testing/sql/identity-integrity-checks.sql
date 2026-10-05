-- Target Service: identity-service
-- Database Engine: H2 (R2DBC Reactive)
-- Status: READ-ONLY (Safe for Production & Staging)
-- Assumptions: Schema contains 'users' table.
-- Execution: Execute via H2 Console or R2DBC client.

-- 1. Check for Duplicate Usernames
SELECT username, COUNT(*) AS duplicate_count
FROM users
GROUP BY username
HAVING COUNT(*) > 1;

-- 2. Verify Active User Status Distribution
SELECT status, COUNT(*) AS total_users
FROM users
GROUP BY status;

-- 3. Check for Orphaned Customer Account References
SELECT user_id, username, customer_id
FROM users
WHERE role = 'CUSTOMER' AND customer_id IS NULL;
