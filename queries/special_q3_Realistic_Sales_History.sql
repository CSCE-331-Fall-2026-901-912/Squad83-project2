-- Special Query 3: Realistic Sales History
-- This query retrieves the hourly sales history, including the hour of the day, order count, and total sales for each hour.
SELECT TO_CHAR(MAKE_TIME(EXTRACT(HOUR FROM order_time)::int, 0, 0), 'FMHH12 AM') AS hour_of_day,
       COUNT(*)                                                               AS order_count,
       SUM(total)                                                             AS hourly_sales
FROM orders
WHERE status = 'completed'
GROUP BY EXTRACT(HOUR FROM order_time)
ORDER BY EXTRACT(HOUR FROM order_time);