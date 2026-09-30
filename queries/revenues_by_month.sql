-- question: how much revenue did the store generate each month, and how many completed orders were placed? --
-- displays all months in order history, and their corresponding revenues, including total tax, total subtotal, total tips, total orders, and total revenue --

SELECT
    DATE_TRUNC('month', order_time)::DATE AS month,
    COUNT(*) AS total_orders,
    SUM(subtotal) AS total_subtotal,
    SUM(tax) AS total_tax,
    SUM(tip) AS total_tips,
    SUM(total) AS total_revenue
FROM orders
WHERE status = 'completed'
GROUP BY DATE_TRUNC('month', order_time)
ORDER BY month;