CREATE TABLE menu_items (
    menu_item_id INT PRIMARY KEY,
    name VARCHAR,
    category menu_category,
    base_price NUMERIC(10,2),
    is_active BOOLEAN
);