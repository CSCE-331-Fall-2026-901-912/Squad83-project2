-- Special Query 2: Peak Sales Day
-- This query retrieves the top 10 days with the highest daily sales.
-- Question: Which 10 days had the highest total sales?
-- Example:  "30 August has $12345 of top sales"
SELECT order_time::date AS sales_day,
       COUNT(*)         AS order_count,
       SUM(total)       AS daily_sales
FROM orders
WHERE status = 'completed'
GROUP BY order_time::date
ORDER BY daily_sales DESC
LIMIT 10;