-- question: which inventory items are at or below their reorder thresholds and need replenishment? --
-- displays all items that are below the order threshold, and by how much --

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