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