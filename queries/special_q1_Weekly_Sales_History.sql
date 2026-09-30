-- Special Query 1: Weekly Sales History
-- This query retrieves the weekly sales history, including the week number, week start date, and order count for each week.
-- Question: Given a specific week, how many orders were placed?
-- Example:  "Week 1 has 98765 orders"
SELECT ROW_NUMBER() OVER (ORDER BY DATE_TRUNC('week', order_time)) AS week_number,
       DATE_TRUNC('week', order_time)::date                        AS week_start,
       COUNT(*)                                                     AS order_count
FROM orders
WHERE status = 'completed'
GROUP BY DATE_TRUNC('week', order_time)
ORDER BY week_number;