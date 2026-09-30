-- How many completed orders did each employee handle, and how much total revenue was associated with those orders?
-- Shows the best-selling menu items based on total quantity sold.
SELECT
    m.menu_item_id,
    m.name,
    SUM(oi.quantity) AS total_quantity_sold
FROM order_items oi
JOIN menu_items m
    ON oi.menu_item_id = m.menu_item_id
JOIN orders o
    ON oi.order_id = o.order_id
WHERE o.status = 'completed'
GROUP BY m.menu_item_id, m.name
ORDER BY total_quantity_sold DESC;