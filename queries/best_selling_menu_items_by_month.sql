-- Question: What are the best-selling menu items for each month?
--
-- Displays the menu item with the highest quantity sold for each month.

SELECT
    TO_CHAR(DATE_TRUNC('month', o.order_time), 'YYYY-MM') AS month,
    m.name AS menu_item,
    SUM(oi.quantity) AS total_quantity_sold_per_month
FROM order_items oi
JOIN menu_items m
    ON oi.menu_item_id = m.menu_item_id
JOIN orders o
    ON oi.order_id = o.order_id
WHERE o.status = 'completed'
GROUP BY DATE_TRUNC('month', o.order_time), m.name
ORDER BY month, total_quantity_sold_per_month DESC;