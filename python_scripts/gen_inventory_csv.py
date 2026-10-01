"""
Generates the seed CSVs for the inventory and menu_item_ingredients tables.
Non-numerical data (item names, recipes) is hardcoded below; run with:  python gen_inventory_csv.py
Outputs: inventory.csv, menu_item_ingredients.csv
"""
import csv

# menu_item_id -> name, matching the team's menu_items table (used for checks + readability)
MENU = {
    1: 'Classic Pearl Milk Tea',
    2: 'Honey Pearl Milk Tea',
    3: 'Coffee Creama',
    4: 'Coffee Milk Tea w/ Coffee Jelly',
    5: 'Hokkaido Pearl Milk Tea',
    6: 'Thai Pearl Milk Tea',
    7: 'Taro Pearl Milk Tea',
    8: 'Mango Green Milk Tea',
    9: 'Golden Retriever',
    10: 'Coconut Pearl Milk Tea',
    11: 'Classic Tea',
    12: 'Honey Tea',
    13: 'Mango Green Tea',
    14: 'Passion Chess',
    15: 'Berry Lychee Burst',
    16: 'Peach Tea w/ Honey Jelly',
    17: 'Mango & Passion Fruit Tea',
    18: 'Honey Lemonade',
    19: 'Tiger Boba',
    20: 'Strawberry Coconut',
    21: 'Strawberry Coconut Ice Blended',
    22: 'Halo Halo',
    23: 'Halo Halo Ice Blended',
    24: 'Wintermelon Lemonade',
    25: 'Wintermelon Lemonade Ice Blended',
    26: 'Wintermelon w/ Fresh Milk',
    27: 'Matcha Pearl Milk Tea',
    28: 'Matcha Fresh Milk',
    29: 'Strawberry Matcha Fresh Milk',
    30: 'Mango Matcha Fresh Milk',
    31: 'Matcha Ice Blended',
    32: 'Oreo w/ Pearl',
    33: 'Taro w/ Pudding',
    34: 'Thai Tea w/ Pearl',
    35: 'Coffee w/ Ice Cream',
    36: 'Mango w/ Ice Cream',
    37: 'Strawberry w/ Lychee Jelly & Ice Cream',
    38: 'Peach Tea w/ Lychee Jelly',
    39: 'Lava Flow',
}

# (inventory_id, name, category, unit, quantity_on_hand, reorder_threshold, unit_cost per unit)
inv = [
 # ---- ingredients ----
 (1,'Black Tea (brewed)','ingredient','oz',1500,400,0.03),
 (2,'Jasmine Green Tea (brewed)','ingredient','oz',1500,400,0.03),
 (3,'Thai Tea (brewed)','ingredient','oz',800,200,0.05),
 (4,'Coffee (brewed)','ingredient','oz',800,200,0.08),
 (5,'Fresh Milk','ingredient','oz',1200,300,0.04),
 (6,'Non-Dairy Creamer','ingredient','g',10000,2500,0.01),
 (7,'Coconut Milk','ingredient','oz',600,150,0.06),
 (8,'Matcha Powder','ingredient','g',3000,750,0.12),
 (9,'Taro Powder','ingredient','g',6000,1500,0.02),
 (10,'Hokkaido Syrup','ingredient','oz',300,75,0.15),
 (11,'Honey','ingredient','oz',400,100,0.12),
 (12,'Cane Sugar Syrup','ingredient','oz',800,200,0.03),
 (13,'Brown Sugar Syrup','ingredient','oz',400,100,0.08),
 (14,'Mango Syrup','ingredient','oz',400,100,0.10),
 (15,'Passion Fruit Syrup','ingredient','oz',300,75,0.10),
 (16,'Strawberry Puree','ingredient','oz',400,100,0.12),
 (17,'Peach Syrup','ingredient','oz',300,75,0.10),
 (18,'Lychee Syrup','ingredient','oz',300,75,0.10),
 (19,'Mixed Berry Syrup','ingredient','oz',300,75,0.10),
 (20,'Wintermelon Syrup','ingredient','oz',400,100,0.09),
 (21,'Lemon Juice','ingredient','oz',400,100,0.07),
 (22,'Pineapple Syrup','ingredient','oz',300,75,0.10),
 (23,'Ube Syrup','ingredient','oz',40,50,0.14),
 (24,'Ice','ingredient','g',200000,50000,0.0002),
 # ---- toppings ----
 (25,'Tapioca Pearls','topping','g',20000,5000,0.004),
 (26,'Honey Jelly','topping','g',8000,2000,0.006),
 (27,'Coffee Jelly','topping','g',6000,1500,0.006),
 (28,'Lychee Jelly','topping','g',8000,2000,0.006),
 (29,'Coconut Jelly','topping','g',8000,2000,0.006),
 (30,'Egg Pudding','topping','g',6000,1500,0.008),
 (31,'Red Bean','topping','g',5000,1250,0.007),
 (32,'Aloe Vera','topping','g',5000,1250,0.007),
 (33,'Crystal Boba','topping','g',6000,1500,0.007),
 (34,'Mango Popping Boba','topping','g',5000,1250,0.009),
 (35,'Strawberry Popping Boba','topping','g',5000,1250,0.009),
 (36,'Vanilla Ice Cream','topping','oz',400,100,0.15),
 (37,'Oreo Cookie Crumbs','topping','g',4000,1000,0.012),
 (38,'Creama (Cheese Foam)','topping','oz',300,75,0.20),
 # ---- single-use supplies ----
 (39,'Cup (16 oz)','supply','each',3000,800,0.09),
 (40,'Cup (24 oz)','supply','each',1500,400,0.12),
 (41,'Sealing Film','supply','each',4000,1000,0.02),
 (42,'Dome Lid','supply','each',1500,400,0.06),
 (43,'Boba Straw','supply','each',4000,1000,0.03),
 (44,'Napkin','supply','each',8000,2000,0.01),
 (45,'Plastic Spoon','supply','each',1500,400,0.02),
 (46,'Paper Bag (Small)','supply','each',1500,400,0.10),
 (47,'Drink Carrier (4-cup)','supply','each',150,200,0.15),
]
by_name = {r[1]: r[0] for r in inv}

# Recipes: amount of each inventory item used per ONE drink, in that item's unit (oz / g / each)
def base(blended=False, spoon=False):
    b = {'Cup (16 oz)':1, 'Boba Straw':1, 'Napkin':1}
    if blended: b.update({'Dome Lid':1, 'Ice':250})
    else:       b.update({'Sealing Film':1, 'Ice':150})
    if spoon: b['Plastic Spoon'] = 1
    return b

MT = {'Non-Dairy Creamer':30}  # milk tea creamer
recipes = {
 1: dict(base(), **{'Black Tea (brewed)':8,'Cane Sugar Syrup':1,'Tapioca Pearls':60}, **MT),
 2: dict(base(), **{'Black Tea (brewed)':8,'Honey':1,'Tapioca Pearls':60}, **MT),
 3: dict(base(), **{'Coffee (brewed)':8,'Fresh Milk':3,'Cane Sugar Syrup':0.75,'Creama (Cheese Foam)':2}),
 4: dict(base(), **{'Black Tea (brewed)':5,'Coffee (brewed)':3,'Cane Sugar Syrup':1,'Coffee Jelly':60}, **MT),
 5: dict(base(), **{'Black Tea (brewed)':8,'Hokkaido Syrup':1,'Tapioca Pearls':60}, **MT),
 6: dict(base(), **{'Thai Tea (brewed)':8,'Cane Sugar Syrup':1,'Tapioca Pearls':60}, **MT),
 7: dict(base(), **{'Taro Powder':30,'Fresh Milk':6,'Non-Dairy Creamer':15,'Cane Sugar Syrup':0.75,'Tapioca Pearls':60}),
 8: dict(base(), **{'Jasmine Green Tea (brewed)':8,'Mango Syrup':1}, **MT),
 9: dict(base(), **{'Black Tea (brewed)':6,'Fresh Milk':4,'Honey':1,'Hokkaido Syrup':0.5,'Crystal Boba':60}),
 10: dict(base(), **{'Black Tea (brewed)':7,'Coconut Milk':3,'Non-Dairy Creamer':15,'Cane Sugar Syrup':1,'Tapioca Pearls':60}),
 11: dict(base(), **{'Black Tea (brewed)':12,'Cane Sugar Syrup':1}),
 12: dict(base(), **{'Black Tea (brewed)':12,'Honey':1.25}),
 13: dict(base(), **{'Jasmine Green Tea (brewed)':10,'Mango Syrup':1.5}),
 14: dict(base(), **{'Jasmine Green Tea (brewed)':8,'Passion Fruit Syrup':1.25,'Crystal Boba':60}),
 15: dict(base(), **{'Jasmine Green Tea (brewed)':8,'Mixed Berry Syrup':1,'Lychee Syrup':0.75,'Strawberry Popping Boba':60}),
 16: dict(base(), **{'Black Tea (brewed)':10,'Peach Syrup':1.25,'Honey Jelly':60}),
 17: dict(base(), **{'Jasmine Green Tea (brewed)':9,'Mango Syrup':0.75,'Passion Fruit Syrup':0.75}),
 18: dict(base(), **{'Lemon Juice':2,'Honey':1.5}),
 19: dict(base(), **{'Fresh Milk':10,'Brown Sugar Syrup':1.5,'Tapioca Pearls':70}),
 20: dict(base(), **{'Strawberry Puree':2,'Coconut Milk':4,'Fresh Milk':3,'Coconut Jelly':60}),
 21: dict(base(True), **{'Strawberry Puree':2,'Coconut Milk':4,'Fresh Milk':2,'Cane Sugar Syrup':0.5}),
 22: dict(base(spoon=True), **{'Ube Syrup':1.5,'Fresh Milk':6,'Coconut Jelly':30,'Red Bean':30,'Egg Pudding':30}),
 23: dict(base(True, True), **{'Ube Syrup':1.5,'Fresh Milk':6,'Coconut Jelly':30,'Red Bean':30,'Egg Pudding':30}),
 24: dict(base(), **{'Wintermelon Syrup':1.5,'Lemon Juice':1.5}),
 25: dict(base(True), **{'Wintermelon Syrup':1.5,'Lemon Juice':1.5}),
 26: dict(base(), **{'Wintermelon Syrup':1.5,'Fresh Milk':8}),
 27: dict(base(), **{'Matcha Powder':4,'Jasmine Green Tea (brewed)':4,'Cane Sugar Syrup':1,'Tapioca Pearls':60}, **MT),
 28: dict(base(), **{'Matcha Powder':4,'Fresh Milk':10,'Cane Sugar Syrup':0.75}),
 29: dict(base(), **{'Matcha Powder':4,'Fresh Milk':9,'Strawberry Puree':1.5}),
 30: dict(base(), **{'Matcha Powder':4,'Fresh Milk':9,'Mango Syrup':1}),
 31: dict(base(True), **{'Matcha Powder':5,'Fresh Milk':6,'Cane Sugar Syrup':1}),
 32: dict(base(True), **{'Fresh Milk':8,'Non-Dairy Creamer':15,'Oreo Cookie Crumbs':25,'Cane Sugar Syrup':0.75,'Tapioca Pearls':60}),
 33: dict(base(True, True), **{'Taro Powder':35,'Fresh Milk':6,'Non-Dairy Creamer':15,'Egg Pudding':60}),
 34: dict(base(True), **{'Thai Tea (brewed)':6,'Cane Sugar Syrup':1,'Tapioca Pearls':60}, **MT),
 35: dict(base(True, True), **{'Coffee (brewed)':6,'Fresh Milk':3,'Cane Sugar Syrup':0.75,'Vanilla Ice Cream':3}),
 36: dict(base(True, True), **{'Mango Syrup':2,'Fresh Milk':3,'Vanilla Ice Cream':3}),
 37: dict(base(True, True), **{'Strawberry Puree':2,'Lychee Jelly':60,'Vanilla Ice Cream':3}),
 38: dict(base(), **{'Black Tea (brewed)':10,'Peach Syrup':1.25,'Lychee Jelly':60}),
 39: dict(base(True), **{'Strawberry Puree':1.5,'Pineapple Syrup':1.5,'Coconut Milk':3,'Mango Popping Boba':60}),
}

assert set(recipes) == set(MENU), "every menu item needs a recipe"
for mid, rec in recipes.items():
    for item in rec:
        assert item in by_name, f"unknown inventory item {item!r} in {MENU[mid]}"

with open("inventory.csv", "w", newline="") as f:
    w = csv.writer(f)
    w.writerow(["inventory_id", "name", "category", "unit", "quantity_on_hand", "reorder_threshold", "unit_cost"])
    w.writerows(inv)

rows = 0
with open("menu_item_ingredients.csv", "w", newline="") as f:
    w = csv.writer(f)
    w.writerow(["menu_item_id", "inventory_id", "quantity_used"])
    for mid in sorted(recipes):
        for item, qty in sorted(recipes[mid].items(), key=lambda kv: by_name[kv[0]]):
            w.writerow([mid, by_name[item], qty])
            rows += 1

print(f"inventory.csv: {len(inv)} rows, menu_item_ingredients.csv: {rows} rows")
