-- Target Service: notification-service
-- Database Engine: H2 (R2DBC Reactive)
-- Status: READ-ONLY (Safe for Production & Staging)
-- Assumptions: Schema contains 'notifications' table.
-- Execution: Execute via H2 Console or R2DBC client.

-- 1. Unread Notification Counts per Recipient User ID
SELECT user_id, COUNT(*) AS unread_count
FROM notifications
WHERE is_read = FALSE
GROUP BY user_id;

-- 2. Audit Notification Distribution by Category
SELECT category, COUNT(*) AS total
FROM notifications
GROUP BY category;
