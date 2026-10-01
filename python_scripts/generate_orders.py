import csv
import random
from datetime import datetime, timedelta

# -----------------------------
# FILES
# -----------------------------

MENU_FILE = "menu_items_with_ids.csv"
ORDERS_FILE = "orders.csv"
ORDER_ITEMS_FILE = "order_items.csv"

# -----------------------------
# PROJECT REQUIREMENTS
# -----------------------------

START_DATE = datetime(2025, 9, 26)
END_DATE = datetime(2026, 9, 25)

TARGET_SALES = 1_000_000

# Three intentionally busy days
PEAK_DAYS = {
    datetime(2026, 1, 20).date(),
    datetime(2026, 8, 24).date(),
    datetime(2026, 8, 25).date()
}

# Cashiers from your employees table
EMPLOYEE_IDS = list(range(3, 13))

TAX_RATE = 0.0825

PAYMENT_METHODS = ["card", "cash", "mobile"]

SIZES = ["M", "L"]

SUGAR_LEVELS = [
    "0%",
    "25%",
    "50%",
    "75%",
    "100%"
]

ICE_LEVELS = [
    "none",
    "less",
    "regular"
]


# -----------------------------
# LOAD MENU ITEMS
# -----------------------------

def load_menu():
    menu = []

    with open(MENU_FILE, newline="", encoding="utf-8") as file:
        reader = csv.DictReader(file)

        for row in reader:
            menu.append({
                "menu_item_id": int(row["menu_item_id"]),
                "name": row["name"],
                "base_price": float(row["base_price"])
            })

    return menu


# -----------------------------
# GENERATE REALISTIC ORDER TIME
# -----------------------------

def random_order_time(date):
    # Repeated hours make some times more likely.
    # Lunch and evening are intentionally busier.

    hours = [
        10,
        11, 11,
        12, 12, 12, 12,
        13, 13, 13,
        14, 14,
        15,
        16,
        17, 17, 17,
        18, 18, 18, 18,
        19, 19, 19,
        20, 20,
        21
    ]

    hour = random.choice(hours)

    minute = random.randint(0, 59)
    second = random.randint(0, 59)

    return datetime(
        date.year,
        date.month,
        date.day,
        hour,
        minute,
        second
    )


# -----------------------------
# CREATE ONE ORDER
# -----------------------------

def create_order(menu, order_id, order_item_id, date):

    # Most customers buy 1-3 drinks.
    # Four-item orders are possible, but less common.
    number_of_items = random.choices(
        [1, 2, 3, 4],
        weights=[40, 35, 20, 5],
        k=1
    )[0]

    selected_items = random.choices(
        menu,
        k=number_of_items
    )

    subtotal = 0

    generated_items = []

    for menu_item in selected_items:

        size = random.choice(SIZES)

        unit_price = menu_item["base_price"]

        # Large drinks cost slightly more.
        if size == "L":
            unit_price += 0.75

        unit_price = round(unit_price, 2)

        subtotal += unit_price

        generated_items.append({
            "order_item_id": order_item_id,
            "order_id": order_id,
            "menu_item_id": menu_item["menu_item_id"],
            "parent_order_item_id": "",
            "quantity": 1,
            "size": size,
            "sugar_level": random.choice(SUGAR_LEVELS),
            "ice_level": random.choice(ICE_LEVELS),
            "unit_price": unit_price
        })

        order_item_id += 1

    subtotal = round(subtotal, 2)

    tax = round(subtotal * TAX_RATE, 2)

    # Many customers give no tip.
    tip_percent = random.choices(
        [0, 0.10, 0.15, 0.20],
        weights=[55, 15, 20, 10],
        k=1
    )[0]

    tip = round(subtotal * tip_percent, 2)

    total = round(
        subtotal + tax + tip,
        2
    )

    order = {
        "order_id": order_id,
        "order_time": random_order_time(date),
        "employee_id": random.choice(EMPLOYEE_IDS),
        "subtotal": subtotal,
        "tax": tax,
        "tip": tip,
        "total": total,
        "payment_method": random.choices(
            PAYMENT_METHODS,
            weights=[65, 20, 15],
            k=1
        )[0],
        "status": "completed"
    }

    return order, generated_items, order_item_id


# -----------------------------
# GENERATE FULL YEAR
# -----------------------------

def generate_data(menu):

    orders = []
    order_items = []

    order_id = 1
    order_item_id = 1

    total_sales = 0

    current_date = START_DATE

    while current_date <= END_DATE:

        date = current_date.date()

        # Normal days
        if current_date.weekday() < 5:
            orders_today = random.randint(110, 160)

        # Weekends
        else:
            orders_today = random.randint(130, 190)

        # Peak days are intentionally much busier
        if date in PEAK_DAYS:
            orders_today = random.randint(400, 500)

        for _ in range(orders_today):

            order, items, order_item_id = create_order(
                menu,
                order_id,
                order_item_id,
                date
            )

            orders.append(order)

            order_items.extend(items)

            total_sales += order["total"]

            order_id += 1

        current_date += timedelta(days=1)

    # If we still have not reached approximately $1M,
    # create additional orders distributed throughout the year.

    while total_sales < TARGET_SALES:

        days_from_start = random.randint(
            0,
            (END_DATE - START_DATE).days
        )

        random_date = (
            START_DATE + timedelta(days=days_from_start)
        ).date()

        order, items, order_item_id = create_order(
            menu,
            order_id,
            order_item_id,
            random_date
        )

        orders.append(order)

        order_items.extend(items)

        total_sales += order["total"]

        order_id += 1

    return orders, order_items, total_sales


# -----------------------------
# WRITE ORDERS CSV
# -----------------------------

def write_orders(orders):

    fieldnames = [
        "order_id",
        "order_time",
        "employee_id",
        "subtotal",
        "tax",
        "tip",
        "total",
        "payment_method",
        "status"
    ]

    with open(
        ORDERS_FILE,
        "w",
        newline="",
        encoding="utf-8"
    ) as file:

        writer = csv.DictWriter(
            file,
            fieldnames=fieldnames
        )

        writer.writeheader()

        writer.writerows(orders)


# -----------------------------
# WRITE ORDER ITEMS CSV
# -----------------------------

def write_order_items(order_items):

    fieldnames = [
        "order_item_id",
        "order_id",
        "menu_item_id",
        "parent_order_item_id",
        "quantity",
        "size",
        "sugar_level",
        "ice_level",
        "unit_price"
    ]

    with open(
        ORDER_ITEMS_FILE,
        "w",
        newline="",
        encoding="utf-8"
    ) as file:

        writer = csv.DictWriter(
            file,
            fieldnames=fieldnames
        )

        writer.writeheader()

        writer.writerows(order_items)


# -----------------------------
# MAIN PROGRAM
# -----------------------------

def main():

    menu = load_menu()

    print(
        f"Loaded {len(menu)} menu items."
    )

    orders, order_items, total_sales = generate_data(menu)

    write_orders(orders)

    write_order_items(order_items)

    print()
    print("Generation complete.")
    print("----------------------------")
    print(f"Orders: {len(orders):,}")
    print(f"Order items: {len(order_items):,}")
    print(f"Total sales: ${total_sales:,.2f}")
    print()
    print(f"Created: {ORDERS_FILE}")
    print(f"Created: {ORDER_ITEMS_FILE}")


if __name__ == "__main__":
    main()