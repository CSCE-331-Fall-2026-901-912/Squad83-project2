-- Question: How much revenue comes from each payment method?
--
-- Displays the total number of orders and total revenue for each payment method.

SELECT
    payment_method,
    COUNT(*) AS total_orders,
    SUM(total) AS total_revenue
FROM orders
GROUP BY payment_method;

SELECT
    o.payment_method,
    COUNT(*) AS total_orders,
    SUM(o.total) AS total_revenue
FROM orders o
WHERE o.status = 'completed'
GROUP BY o.payment_method
ORDER BY total_revenue DESC;