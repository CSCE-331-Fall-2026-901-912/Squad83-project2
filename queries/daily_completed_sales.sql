-- How many completed orders were made each day, and how much total revenue did each day generate?
-- Shows total completed sales by day.
SELECT
    DATE(order_time) AS sale_date,
    COUNT(*) AS total_orders,
    SUM(total) AS total_sales
FROM orders
WHERE status = 'completed'
GROUP BY DATE(order_time)
ORDER BY sale_date;
