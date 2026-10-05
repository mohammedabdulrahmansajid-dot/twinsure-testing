-- Target Service: customer-service
-- Database Engine: H2 (R2DBC Reactive)
-- Status: READ-ONLY (Safe for Production & Staging)
-- Assumptions: Schema contains 'customers' table.
-- Execution: Execute via H2 Console or R2DBC client.

-- 1. Check for Duplicate User ID Ownership
SELECT user_id, COUNT(*) AS customer_count
FROM customers
GROUP BY user_id
HAVING COUNT(*) > 1;

-- 2. Check for Duplicate Email Addresses
SELECT email, COUNT(*) AS duplicate_count
FROM customers
GROUP BY email
HAVING COUNT(*) > 1;

-- 3. Audit Active vs Suspended Customers
SELECT status, COUNT(*) AS total
FROM customers
GROUP BY status;
