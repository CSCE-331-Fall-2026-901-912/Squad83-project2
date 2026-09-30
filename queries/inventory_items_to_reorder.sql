-- displays all items that are below the reorder threshold, and by how much --

SELECT
    inventory_id,
    name,
    category,
    unit,
    quantity_on_hand,
    reorder_threshold,
    unit_cost,
    (reorder_threshold - quantity_on_hand) AS below_threshold_by
FROM inventory
WHERE quantity_on_hand <= reorder_threshold
ORDER BY below_threshold_by DESC;