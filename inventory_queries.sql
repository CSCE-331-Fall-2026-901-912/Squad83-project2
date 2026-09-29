SELECT m.name AS menu_item, COUNT(mii.inventory_id) AS inventory_items_used
FROM menu_items m
JOIN menu_item_ingredients mii ON mii.menu_item_id = m.menu_item_id
GROUP BY m.menu_item_id, m.name
ORDER BY m.menu_item_id;


SELECT category, COUNT(*) AS item_count
FROM inventory
GROUP BY category
ORDER BY category;


SELECT name, quantity_on_hand, reorder_threshold, unit
FROM inventory
WHERE quantity_on_hand <= reorder_threshold
ORDER BY name;


SELECT m.name AS menu_item,
       ROUND(SUM(mii.quantity_used * i.unit_cost)::numeric, 2) AS cost_to_make,
       m.base_price
FROM menu_items m
JOIN menu_item_ingredients mii ON mii.menu_item_id = m.menu_item_id
JOIN inventory i               ON i.inventory_id   = mii.inventory_id
GROUP BY m.menu_item_id, m.name, m.base_price
ORDER BY cost_to_make DESC;


SELECT i.name, i.quantity_on_hand, i.unit,
       ROUND(AVG(mii.quantity_used)::numeric, 2)                     AS avg_amount_per_drink,
       FLOOR(i.quantity_on_hand / AVG(mii.quantity_used))            AS servings_left,
       COUNT(*)                                                      AS drinks_using_it
FROM inventory i
JOIN menu_item_ingredients mii ON mii.inventory_id = i.inventory_id
WHERE i.category <> 'supply'
GROUP BY i.inventory_id, i.name, i.quantity_on_hand, i.unit
ORDER BY servings_left;


SELECT i.name AS inventory_item, mii.quantity_used, i.unit, i.category
FROM menu_item_ingredients mii
JOIN menu_items m ON m.menu_item_id = mii.menu_item_id
JOIN inventory  i ON i.inventory_id = mii.inventory_id
WHERE m.name = 'Classic Pearl Milk Tea'
ORDER BY i.category, i.name;
