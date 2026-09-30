-- question: based on completed sales, how much of each inventory item has been used in preparing drinks? --
-- gives estimate of inventory items used from recipes and drink sales --

SELECT
    i.inventory_id,
    i.name AS inventory_item,
    i.unit,
    SUM(oi.quantity * mi.quantity_used) AS estimated_quantity_used
FROM orders o
JOIN order_items oi
    ON o.order_id = oi.order_id
JOIN menu_item_ingredients mi
    ON oi.menu_item_id = mi.menu_item_id
JOIN inventory i
    ON mi.inventory_id = i.inventory_id
WHERE o.status = 'completed'
  AND oi.parent_order_item_id IS NULL
GROUP BY i.inventory_id, i.name, i.unit
ORDER BY estimated_quantity_used DESC;