-- Special Query #4: "Menu Item Inventory"
-- pseudocode: select count of inventory items from inventory and menu grouped by menu item
-- Q: Given a specific menu item, how many items from the inventory does that menu item use?
-- e.g. "Classic Pearl Milk Tea uses 9 items"
SELECT m.menu_item_id,
       m.name                   AS menu_item,
       COUNT(mii.inventory_id)  AS inventory_items_used
FROM menu_items m
JOIN menu_item_ingredients mii ON mii.menu_item_id = m.menu_item_id
JOIN inventory i               ON i.inventory_id   = mii.inventory_id
GROUP BY m.menu_item_id, m.name
ORDER BY m.menu_item_id;
