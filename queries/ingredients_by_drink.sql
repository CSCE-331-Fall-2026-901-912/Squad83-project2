-- question: what ingredients are used in each menu item, and how much of each ingredient is required per serving? --
-- displays every drink with their corresponding ingredients, as well as the portions of ingredients used for each drink --

SELECT
    m.name AS menu_item,
    i.name AS ingredient,
    i.category AS inventory_category,
    mi.quantity_used,
    i.unit
FROM menu_items m
JOIN menu_item_ingredients mi
    ON m.menu_item_id = mi.menu_item_id
JOIN inventory i
    ON mi.inventory_id = i.inventory_id
ORDER BY m.name, i.name;