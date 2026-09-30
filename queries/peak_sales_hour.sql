-- Question: What hours have the most sales?
--
-- Displays the total number of orders for each hour of the day.

SELECT
    EXTRACT(HOUR FROM o.order_time) AS order_hour,
    COUNT(*) AS total_orders
FROM orders o
WHERE o.status = 'completed'
GROUP BY EXTRACT(HOUR FROM o.order_time)
ORDER BY total_orders DESC;