-- Shows completed sales grouped by payment method.

SELECT
    o.payment_method,
    COUNT(*) AS total_orders,
    SUM(o.total) AS total_revenue
FROM orders o
WHERE o.status = 'completed'
GROUP BY o.payment_method
ORDER BY total_revenue DESC;