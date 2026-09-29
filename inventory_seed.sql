-- ============================================================
-- Seeds the inventory and menu_item_ingredients tables from CSV.
-- CSVs are produced by gen_inventory_csv.py.
-- Run from psql AFTER moving into this folder:
--     \cd 'C:/path/to/this/folder'
--     \i inventory_seed.sql
-- Requires menu_items to already be seeded.
-- One transaction: if anything fails, nothing is saved.
-- ============================================================
BEGIN;

DROP TABLE IF EXISTS menu_item_ingredients;
DROP TABLE IF EXISTS inventory;

CREATE TABLE inventory (
    inventory_id      INT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL UNIQUE,
    category          VARCHAR(20)  NOT NULL CHECK (category IN ('ingredient','topping','supply')),
    unit              VARCHAR(10)  NOT NULL CHECK (unit IN ('oz','g','each')),
    quantity_on_hand  DOUBLE PRECISION NOT NULL CHECK (quantity_on_hand >= 0),
    reorder_threshold DOUBLE PRECISION NOT NULL CHECK (reorder_threshold >= 0),
    unit_cost         DOUBLE PRECISION NOT NULL CHECK (unit_cost >= 0)  -- cost per 1 unit (oz / g / each)
);

CREATE TABLE menu_item_ingredients (
    menu_item_id  INT NOT NULL REFERENCES menu_items(menu_item_id),
    inventory_id  INT NOT NULL REFERENCES inventory(inventory_id),
    quantity_used DOUBLE PRECISION NOT NULL CHECK (quantity_used > 0),  -- per one drink, in the item's unit
    PRIMARY KEY (menu_item_id, inventory_id)
);

\copy inventory FROM 'inventory.csv' WITH (FORMAT csv, HEADER true)
\copy menu_item_ingredients FROM 'menu_item_ingredients.csv' WITH (FORMAT csv, HEADER true)

COMMIT;

-- Evidence of seeding
SELECT 'inventory' AS table_name, COUNT(*) AS row_count FROM inventory
UNION ALL
SELECT 'menu_item_ingredients', COUNT(*) FROM menu_item_ingredients;
