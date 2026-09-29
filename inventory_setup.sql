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
    unit_cost         DOUBLE PRECISION NOT NULL CHECK (unit_cost >= 0)  -- cost per 1 unit (per oz / g / each)
);

CREATE TABLE menu_item_ingredients (
    menu_item_id  INT NOT NULL REFERENCES menu_items(menu_item_id),
    inventory_id  INT NOT NULL REFERENCES inventory(inventory_id),
    quantity_used DOUBLE PRECISION NOT NULL CHECK (quantity_used > 0),  -- in the inventory item's unit, per one drink
    PRIMARY KEY (menu_item_id, inventory_id)
);


INSERT INTO inventory (inventory_id, name, category, unit, quantity_on_hand, reorder_threshold, unit_cost) VALUES
    (1, 'Black Tea (brewed)', 'ingredient', 'oz', 1500, 400, 0.03),
    (2, 'Jasmine Green Tea (brewed)', 'ingredient', 'oz', 1500, 400, 0.03),
    (3, 'Thai Tea (brewed)', 'ingredient', 'oz', 800, 200, 0.05),
    (4, 'Coffee (brewed)', 'ingredient', 'oz', 800, 200, 0.08),
    (5, 'Fresh Milk', 'ingredient', 'oz', 1200, 300, 0.04),
    (6, 'Non-Dairy Creamer', 'ingredient', 'g', 10000, 2500, 0.01),
    (7, 'Coconut Milk', 'ingredient', 'oz', 600, 150, 0.06),
    (8, 'Matcha Powder', 'ingredient', 'g', 3000, 750, 0.12),
    (9, 'Taro Powder', 'ingredient', 'g', 6000, 1500, 0.02),
    (10, 'Hokkaido Syrup', 'ingredient', 'oz', 300, 75, 0.15),
    (11, 'Honey', 'ingredient', 'oz', 400, 100, 0.12),
    (12, 'Cane Sugar Syrup', 'ingredient', 'oz', 800, 200, 0.03),
    (13, 'Brown Sugar Syrup', 'ingredient', 'oz', 400, 100, 0.08),
    (14, 'Mango Syrup', 'ingredient', 'oz', 400, 100, 0.1),
    (15, 'Passion Fruit Syrup', 'ingredient', 'oz', 300, 75, 0.1),
    (16, 'Strawberry Puree', 'ingredient', 'oz', 400, 100, 0.12),
    (17, 'Peach Syrup', 'ingredient', 'oz', 300, 75, 0.1),
    (18, 'Lychee Syrup', 'ingredient', 'oz', 300, 75, 0.1),
    (19, 'Mixed Berry Syrup', 'ingredient', 'oz', 300, 75, 0.1),
    (20, 'Wintermelon Syrup', 'ingredient', 'oz', 400, 100, 0.09),
    (21, 'Lemon Juice', 'ingredient', 'oz', 400, 100, 0.07),
    (22, 'Pineapple Syrup', 'ingredient', 'oz', 300, 75, 0.1),
    (23, 'Ube Syrup', 'ingredient', 'oz', 40, 50, 0.14),
    (24, 'Ice', 'ingredient', 'g', 200000, 50000, 0.0002),
    (25, 'Tapioca Pearls', 'topping', 'g', 20000, 5000, 0.004),
    (26, 'Honey Jelly', 'topping', 'g', 8000, 2000, 0.006),
    (27, 'Coffee Jelly', 'topping', 'g', 6000, 1500, 0.006),
    (28, 'Lychee Jelly', 'topping', 'g', 8000, 2000, 0.006),
    (29, 'Coconut Jelly', 'topping', 'g', 8000, 2000, 0.006),
    (30, 'Egg Pudding', 'topping', 'g', 6000, 1500, 0.008),
    (31, 'Red Bean', 'topping', 'g', 5000, 1250, 0.007),
    (32, 'Aloe Vera', 'topping', 'g', 5000, 1250, 0.007),
    (33, 'Crystal Boba', 'topping', 'g', 6000, 1500, 0.007),
    (34, 'Mango Popping Boba', 'topping', 'g', 5000, 1250, 0.009),
    (35, 'Strawberry Popping Boba', 'topping', 'g', 5000, 1250, 0.009),
    (36, 'Vanilla Ice Cream', 'topping', 'oz', 400, 100, 0.15),
    (37, 'Oreo Cookie Crumbs', 'topping', 'g', 4000, 1000, 0.012),
    (38, 'Creama (Cheese Foam)', 'topping', 'oz', 300, 75, 0.2),
    (39, 'Cup (16 oz)', 'supply', 'each', 3000, 800, 0.09),
    (40, 'Cup (24 oz)', 'supply', 'each', 1500, 400, 0.12),
    (41, 'Sealing Film', 'supply', 'each', 4000, 1000, 0.02),
    (42, 'Dome Lid', 'supply', 'each', 1500, 400, 0.06),
    (43, 'Boba Straw', 'supply', 'each', 4000, 1000, 0.03),
    (44, 'Napkin', 'supply', 'each', 8000, 2000, 0.01),
    (45, 'Plastic Spoon', 'supply', 'each', 1500, 400, 0.02),
    (46, 'Paper Bag (Small)', 'supply', 'each', 1500, 400, 0.1),
    (47, 'Drink Carrier (4-cup)', 'supply', 'each', 150, 200, 0.15);


INSERT INTO menu_item_ingredients (menu_item_id, inventory_id, quantity_used) VALUES
    (1, 1, 8),  -- Classic Pearl Milk Tea: Black Tea (brewed)
    (1, 6, 30),  -- Non-Dairy Creamer
    (1, 12, 1),  -- Cane Sugar Syrup
    (1, 24, 150),  -- Ice
    (1, 25, 60),  -- Tapioca Pearls
    (1, 39, 1),  -- Cup (16 oz)
    (1, 41, 1),  -- Sealing Film
    (1, 43, 1),  -- Boba Straw
    (1, 44, 1),  -- Napkin
    (2, 1, 8),  -- Honey Pearl Milk Tea: Black Tea (brewed)
    (2, 6, 30),  -- Non-Dairy Creamer
    (2, 11, 1),  -- Honey
    (2, 24, 150),  -- Ice
    (2, 25, 60),  -- Tapioca Pearls
    (2, 39, 1),  -- Cup (16 oz)
    (2, 41, 1),  -- Sealing Film
    (2, 43, 1),  -- Boba Straw
    (2, 44, 1),  -- Napkin
    (3, 4, 8),  -- Coffee Creama: Coffee (brewed)
    (3, 5, 3),  -- Fresh Milk
    (3, 12, 0.75),  -- Cane Sugar Syrup
    (3, 24, 150),  -- Ice
    (3, 38, 2),  -- Creama (Cheese Foam)
    (3, 39, 1),  -- Cup (16 oz)
    (3, 41, 1),  -- Sealing Film
    (3, 43, 1),  -- Boba Straw
    (3, 44, 1),  -- Napkin
    (4, 1, 5),  -- Coffee Milk Tea w/ Coffee Jelly: Black Tea (brewed)
    (4, 4, 3),  -- Coffee (brewed)
    (4, 6, 30),  -- Non-Dairy Creamer
    (4, 12, 1),  -- Cane Sugar Syrup
    (4, 24, 150),  -- Ice
    (4, 27, 60),  -- Coffee Jelly
    (4, 39, 1),  -- Cup (16 oz)
    (4, 41, 1),  -- Sealing Film
    (4, 43, 1),  -- Boba Straw
    (4, 44, 1),  -- Napkin
    (5, 1, 8),  -- Hokkaido Pearl Milk Tea: Black Tea (brewed)
    (5, 6, 30),  -- Non-Dairy Creamer
    (5, 10, 1),  -- Hokkaido Syrup
    (5, 24, 150),  -- Ice
    (5, 25, 60),  -- Tapioca Pearls
    (5, 39, 1),  -- Cup (16 oz)
    (5, 41, 1),  -- Sealing Film
    (5, 43, 1),  -- Boba Straw
    (5, 44, 1),  -- Napkin
    (6, 3, 8),  -- Thai Pearl Milk Tea: Thai Tea (brewed)
    (6, 6, 30),  -- Non-Dairy Creamer
    (6, 12, 1),  -- Cane Sugar Syrup
    (6, 24, 150),  -- Ice
    (6, 25, 60),  -- Tapioca Pearls
    (6, 39, 1),  -- Cup (16 oz)
    (6, 41, 1),  -- Sealing Film
    (6, 43, 1),  -- Boba Straw
    (6, 44, 1),  -- Napkin
    (7, 5, 6),  -- Taro Pearl Milk Tea: Fresh Milk
    (7, 6, 15),  -- Non-Dairy Creamer
    (7, 9, 30),  -- Taro Powder
    (7, 12, 0.75),  -- Cane Sugar Syrup
    (7, 24, 150),  -- Ice
    (7, 25, 60),  -- Tapioca Pearls
    (7, 39, 1),  -- Cup (16 oz)
    (7, 41, 1),  -- Sealing Film
    (7, 43, 1),  -- Boba Straw
    (7, 44, 1),  -- Napkin
    (8, 2, 8),  -- Mango Green Milk Tea: Jasmine Green Tea (brewed)
    (8, 6, 30),  -- Non-Dairy Creamer
    (8, 14, 1),  -- Mango Syrup
    (8, 24, 150),  -- Ice
    (8, 39, 1),  -- Cup (16 oz)
    (8, 41, 1),  -- Sealing Film
    (8, 43, 1),  -- Boba Straw
    (8, 44, 1),  -- Napkin
    (9, 1, 6),  -- Golden Retriever: Black Tea (brewed)
    (9, 5, 4),  -- Fresh Milk
    (9, 10, 0.5),  -- Hokkaido Syrup
    (9, 11, 1),  -- Honey
    (9, 24, 150),  -- Ice
    (9, 33, 60),  -- Crystal Boba
    (9, 39, 1),  -- Cup (16 oz)
    (9, 41, 1),  -- Sealing Film
    (9, 43, 1),  -- Boba Straw
    (9, 44, 1),  -- Napkin
    (10, 1, 7),  -- Coconut Pearl Milk Tea: Black Tea (brewed)
    (10, 6, 15),  -- Non-Dairy Creamer
    (10, 7, 3),  -- Coconut Milk
    (10, 12, 1),  -- Cane Sugar Syrup
    (10, 24, 150),  -- Ice
    (10, 25, 60),  -- Tapioca Pearls
    (10, 39, 1),  -- Cup (16 oz)
    (10, 41, 1),  -- Sealing Film
    (10, 43, 1),  -- Boba Straw
    (10, 44, 1),  -- Napkin
    (11, 1, 12),  -- Classic Tea: Black Tea (brewed)
    (11, 12, 1),  -- Cane Sugar Syrup
    (11, 24, 150),  -- Ice
    (11, 39, 1),  -- Cup (16 oz)
    (11, 41, 1),  -- Sealing Film
    (11, 43, 1),  -- Boba Straw
    (11, 44, 1),  -- Napkin
    (12, 1, 12),  -- Honey Tea: Black Tea (brewed)
    (12, 11, 1.25),  -- Honey
    (12, 24, 150),  -- Ice
    (12, 39, 1),  -- Cup (16 oz)
    (12, 41, 1),  -- Sealing Film
    (12, 43, 1),  -- Boba Straw
    (12, 44, 1),  -- Napkin
    (13, 2, 10),  -- Mango Green Tea: Jasmine Green Tea (brewed)
    (13, 14, 1.5),  -- Mango Syrup
    (13, 24, 150),  -- Ice
    (13, 39, 1),  -- Cup (16 oz)
    (13, 41, 1),  -- Sealing Film
    (13, 43, 1),  -- Boba Straw
    (13, 44, 1),  -- Napkin
    (14, 2, 8),  -- Passion Chess: Jasmine Green Tea (brewed)
    (14, 15, 1.25),  -- Passion Fruit Syrup
    (14, 24, 150),  -- Ice
    (14, 33, 60),  -- Crystal Boba
    (14, 39, 1),  -- Cup (16 oz)
    (14, 41, 1),  -- Sealing Film
    (14, 43, 1),  -- Boba Straw
    (14, 44, 1),  -- Napkin
    (15, 2, 8),  -- Berry Lychee Burst: Jasmine Green Tea (brewed)
    (15, 18, 0.75),  -- Lychee Syrup
    (15, 19, 1),  -- Mixed Berry Syrup
    (15, 24, 150),  -- Ice
    (15, 35, 60),  -- Strawberry Popping Boba
    (15, 39, 1),  -- Cup (16 oz)
    (15, 41, 1),  -- Sealing Film
    (15, 43, 1),  -- Boba Straw
    (15, 44, 1),  -- Napkin
    (16, 1, 10),  -- Peach Tea w/ Honey Jelly: Black Tea (brewed)
    (16, 17, 1.25),  -- Peach Syrup
    (16, 24, 150),  -- Ice
    (16, 26, 60),  -- Honey Jelly
    (16, 39, 1),  -- Cup (16 oz)
    (16, 41, 1),  -- Sealing Film
    (16, 43, 1),  -- Boba Straw
    (16, 44, 1),  -- Napkin
    (17, 2, 9),  -- Mango & Passion Fruit Tea: Jasmine Green Tea (brewed)
    (17, 14, 0.75),  -- Mango Syrup
    (17, 15, 0.75),  -- Passion Fruit Syrup
    (17, 24, 150),  -- Ice
    (17, 39, 1),  -- Cup (16 oz)
    (17, 41, 1),  -- Sealing Film
    (17, 43, 1),  -- Boba Straw
    (17, 44, 1),  -- Napkin
    (18, 11, 1.5),  -- Honey Lemonade: Honey
    (18, 21, 2),  -- Lemon Juice
    (18, 24, 150),  -- Ice
    (18, 39, 1),  -- Cup (16 oz)
    (18, 41, 1),  -- Sealing Film
    (18, 43, 1),  -- Boba Straw
    (18, 44, 1),  -- Napkin
    (19, 5, 10),  -- Tiger Boba: Fresh Milk
    (19, 13, 1.5),  -- Brown Sugar Syrup
    (19, 24, 150),  -- Ice
    (19, 25, 70),  -- Tapioca Pearls
    (19, 39, 1),  -- Cup (16 oz)
    (19, 41, 1),  -- Sealing Film
    (19, 43, 1),  -- Boba Straw
    (19, 44, 1),  -- Napkin
    (20, 5, 3),  -- Strawberry Coconut: Fresh Milk
    (20, 7, 4),  -- Coconut Milk
    (20, 16, 2),  -- Strawberry Puree
    (20, 24, 150),  -- Ice
    (20, 29, 60),  -- Coconut Jelly
    (20, 39, 1),  -- Cup (16 oz)
    (20, 41, 1),  -- Sealing Film
    (20, 43, 1),  -- Boba Straw
    (20, 44, 1),  -- Napkin
    (21, 5, 2),  -- Strawberry Coconut Ice Blended: Fresh Milk
    (21, 7, 4),  -- Coconut Milk
    (21, 12, 0.5),  -- Cane Sugar Syrup
    (21, 16, 2),  -- Strawberry Puree
    (21, 24, 250),  -- Ice
    (21, 39, 1),  -- Cup (16 oz)
    (21, 42, 1),  -- Dome Lid
    (21, 43, 1),  -- Boba Straw
    (21, 44, 1),  -- Napkin
    (22, 5, 6),  -- Halo Halo: Fresh Milk
    (22, 23, 1.5),  -- Ube Syrup
    (22, 24, 150),  -- Ice
    (22, 29, 30),  -- Coconut Jelly
    (22, 30, 30),  -- Egg Pudding
    (22, 31, 30),  -- Red Bean
    (22, 39, 1),  -- Cup (16 oz)
    (22, 41, 1),  -- Sealing Film
    (22, 43, 1),  -- Boba Straw
    (22, 44, 1),  -- Napkin
    (22, 45, 1),  -- Plastic Spoon
    (23, 5, 6),  -- Halo Halo Ice Blended: Fresh Milk
    (23, 23, 1.5),  -- Ube Syrup
    (23, 24, 250),  -- Ice
    (23, 29, 30),  -- Coconut Jelly
    (23, 30, 30),  -- Egg Pudding
    (23, 31, 30),  -- Red Bean
    (23, 39, 1),  -- Cup (16 oz)
    (23, 42, 1),  -- Dome Lid
    (23, 43, 1),  -- Boba Straw
    (23, 44, 1),  -- Napkin
    (23, 45, 1),  -- Plastic Spoon
    (24, 20, 1.5),  -- Wintermelon Lemonade: Wintermelon Syrup
    (24, 21, 1.5),  -- Lemon Juice
    (24, 24, 150),  -- Ice
    (24, 39, 1),  -- Cup (16 oz)
    (24, 41, 1),  -- Sealing Film
    (24, 43, 1),  -- Boba Straw
    (24, 44, 1),  -- Napkin
    (25, 20, 1.5),  -- Wintermelon Lemonade Ice Blended: Wintermelon Syrup
    (25, 21, 1.5),  -- Lemon Juice
    (25, 24, 250),  -- Ice
    (25, 39, 1),  -- Cup (16 oz)
    (25, 42, 1),  -- Dome Lid
    (25, 43, 1),  -- Boba Straw
    (25, 44, 1),  -- Napkin
    (26, 5, 8),  -- Wintermelon w/ Fresh Milk: Fresh Milk
    (26, 20, 1.5),  -- Wintermelon Syrup
    (26, 24, 150),  -- Ice
    (26, 39, 1),  -- Cup (16 oz)
    (26, 41, 1),  -- Sealing Film
    (26, 43, 1),  -- Boba Straw
    (26, 44, 1),  -- Napkin
    (27, 2, 4),  -- Matcha Pearl Milk Tea: Jasmine Green Tea (brewed)
    (27, 6, 30),  -- Non-Dairy Creamer
    (27, 8, 4),  -- Matcha Powder
    (27, 12, 1),  -- Cane Sugar Syrup
    (27, 24, 150),  -- Ice
    (27, 25, 60),  -- Tapioca Pearls
    (27, 39, 1),  -- Cup (16 oz)
    (27, 41, 1),  -- Sealing Film
    (27, 43, 1),  -- Boba Straw
    (27, 44, 1),  -- Napkin
    (28, 5, 10),  -- Matcha Fresh Milk: Fresh Milk
    (28, 8, 4),  -- Matcha Powder
    (28, 12, 0.75),  -- Cane Sugar Syrup
    (28, 24, 150),  -- Ice
    (28, 39, 1),  -- Cup (16 oz)
    (28, 41, 1),  -- Sealing Film
    (28, 43, 1),  -- Boba Straw
    (28, 44, 1),  -- Napkin
    (29, 5, 9),  -- Strawberry Matcha Fresh Milk: Fresh Milk
    (29, 8, 4),  -- Matcha Powder
    (29, 16, 1.5),  -- Strawberry Puree
    (29, 24, 150),  -- Ice
    (29, 39, 1),  -- Cup (16 oz)
    (29, 41, 1),  -- Sealing Film
    (29, 43, 1),  -- Boba Straw
    (29, 44, 1),  -- Napkin
    (30, 5, 9),  -- Mango Matcha Fresh Milk: Fresh Milk
    (30, 8, 4),  -- Matcha Powder
    (30, 14, 1),  -- Mango Syrup
    (30, 24, 150),  -- Ice
    (30, 39, 1),  -- Cup (16 oz)
    (30, 41, 1),  -- Sealing Film
    (30, 43, 1),  -- Boba Straw
    (30, 44, 1),  -- Napkin
    (31, 5, 6),  -- Matcha Ice Blended: Fresh Milk
    (31, 8, 5),  -- Matcha Powder
    (31, 12, 1),  -- Cane Sugar Syrup
    (31, 24, 250),  -- Ice
    (31, 39, 1),  -- Cup (16 oz)
    (31, 42, 1),  -- Dome Lid
    (31, 43, 1),  -- Boba Straw
    (31, 44, 1),  -- Napkin
    (32, 5, 8),  -- Oreo w/ Pearl: Fresh Milk
    (32, 6, 15),  -- Non-Dairy Creamer
    (32, 12, 0.75),  -- Cane Sugar Syrup
    (32, 24, 250),  -- Ice
    (32, 25, 60),  -- Tapioca Pearls
    (32, 37, 25),  -- Oreo Cookie Crumbs
    (32, 39, 1),  -- Cup (16 oz)
    (32, 42, 1),  -- Dome Lid
    (32, 43, 1),  -- Boba Straw
    (32, 44, 1),  -- Napkin
    (33, 5, 6),  -- Taro w/ Pudding: Fresh Milk
    (33, 6, 15),  -- Non-Dairy Creamer
    (33, 9, 35),  -- Taro Powder
    (33, 24, 250),  -- Ice
    (33, 30, 60),  -- Egg Pudding
    (33, 39, 1),  -- Cup (16 oz)
    (33, 42, 1),  -- Dome Lid
    (33, 43, 1),  -- Boba Straw
    (33, 44, 1),  -- Napkin
    (33, 45, 1),  -- Plastic Spoon
    (34, 3, 6),  -- Thai Tea w/ Pearl: Thai Tea (brewed)
    (34, 6, 30),  -- Non-Dairy Creamer
    (34, 12, 1),  -- Cane Sugar Syrup
    (34, 24, 250),  -- Ice
    (34, 25, 60),  -- Tapioca Pearls
    (34, 39, 1),  -- Cup (16 oz)
    (34, 42, 1),  -- Dome Lid
    (34, 43, 1),  -- Boba Straw
    (34, 44, 1),  -- Napkin
    (35, 4, 6),  -- Coffee w/ Ice Cream: Coffee (brewed)
    (35, 5, 3),  -- Fresh Milk
    (35, 12, 0.75),  -- Cane Sugar Syrup
    (35, 24, 250),  -- Ice
    (35, 36, 3),  -- Vanilla Ice Cream
    (35, 39, 1),  -- Cup (16 oz)
    (35, 42, 1),  -- Dome Lid
    (35, 43, 1),  -- Boba Straw
    (35, 44, 1),  -- Napkin
    (35, 45, 1),  -- Plastic Spoon
    (36, 5, 3),  -- Mango w/ Ice Cream: Fresh Milk
    (36, 14, 2),  -- Mango Syrup
    (36, 24, 250),  -- Ice
    (36, 36, 3),  -- Vanilla Ice Cream
    (36, 39, 1),  -- Cup (16 oz)
    (36, 42, 1),  -- Dome Lid
    (36, 43, 1),  -- Boba Straw
    (36, 44, 1),  -- Napkin
    (36, 45, 1),  -- Plastic Spoon
    (37, 16, 2),  -- Strawberry w/ Lychee Jelly & Ice Cream: Strawberry Puree
    (37, 24, 250),  -- Ice
    (37, 28, 60),  -- Lychee Jelly
    (37, 36, 3),  -- Vanilla Ice Cream
    (37, 39, 1),  -- Cup (16 oz)
    (37, 42, 1),  -- Dome Lid
    (37, 43, 1),  -- Boba Straw
    (37, 44, 1),  -- Napkin
    (37, 45, 1),  -- Plastic Spoon
    (38, 1, 10),  -- Peach Tea w/ Lychee Jelly: Black Tea (brewed)
    (38, 17, 1.25),  -- Peach Syrup
    (38, 24, 150),  -- Ice
    (38, 28, 60),  -- Lychee Jelly
    (38, 39, 1),  -- Cup (16 oz)
    (38, 41, 1),  -- Sealing Film
    (38, 43, 1),  -- Boba Straw
    (38, 44, 1),  -- Napkin
    (39, 7, 3),  -- Lava Flow: Coconut Milk
    (39, 16, 1.5),  -- Strawberry Puree
    (39, 22, 1.5),  -- Pineapple Syrup
    (39, 24, 250),  -- Ice
    (39, 34, 60),  -- Mango Popping Boba
    (39, 39, 1),  -- Cup (16 oz)
    (39, 42, 1),  -- Dome Lid
    (39, 43, 1),  -- Boba Straw
    (39, 44, 1);  -- Napkin

COMMIT;
