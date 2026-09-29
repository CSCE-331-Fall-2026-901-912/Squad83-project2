-- Special Query #5: "Best of the Worst"
-- pseudocode: select bottom sum of order total, top count of menu items by day grouped by week
-- Q: Given a specific week, what day had the lowest sales, what were the sales numbers,
--    and what was the top seller that day?
-- e.g. "12 December has lowest sales of $4321 for week 18 with Matcha Pearl Milk Tea as top seller"
WITH daily_sales AS (          -- total sales for every day
    SELECT sale_date::date                      AS sale_day,
           DATE_TRUNC('week', sale_date)::date  AS week_start,
           SUM(quantity * price)                AS day_total
    FROM sales_history
    GROUP BY 1, 2
),
worst_day AS (                 -- lowest-sales day in each week
    SELECT DISTINCT ON (week_start) week_start, sale_day, day_total
    FROM daily_sales
    ORDER BY week_start, day_total ASC, sale_day
),
item_counts AS (               -- how many of each menu item sold on each day
    SELECT s.sale_date::date AS sale_day, m.name AS menu_item, SUM(s.quantity) AS qty_sold
    FROM sales_history s
    JOIN menu_items m ON m.menu_item_id = s.item_id
    GROUP BY 1, 2
),
top_seller AS (                -- best-selling item on each day
    SELECT DISTINCT ON (sale_day) sale_day, menu_item, qty_sold
    FROM item_counts
    ORDER BY sale_day, qty_sold DESC, menu_item
)
SELECT DENSE_RANK() OVER (ORDER BY w.week_start) AS week_number,
       w.week_start,
       TO_CHAR(w.sale_day, 'Dy DD Mon YYYY')     AS lowest_sales_day,
       ROUND(w.day_total, 2)                     AS lowest_day_sales,
       t.menu_item                               AS top_seller,
       t.qty_sold                                AS top_seller_qty
FROM worst_day w
JOIN top_seller t ON t.sale_day = w.sale_day
ORDER BY w.week_start;
