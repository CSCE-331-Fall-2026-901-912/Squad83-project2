-- Q: Over the full sales history, how much of each inventory item was used to make the drinks we sold?
--    (combines sales_history with each drink's recipe in menu_item_ingredients)
-- e.g. "Tapioca Pearls: 812,340 g used across 13,539 drinks"
SELECT i.name                                   AS inventory_item,
       i.category,
       ROUND(SUM(s.quantity * mii.quantity_used)::numeric, 1) AS total_used,
       i.unit,
       SUM(s.quantity)                          AS drinks_made_with_it
FROM sales_history s
JOIN menu_item_ingredients mii ON mii.menu_item_id = s.item_id
JOIN inventory i               ON i.inventory_id   = mii.inventory_id
GROUP BY i.inventory_id, i.name, i.category, i.unit
ORDER BY i.category, total_used DESC;
