-- Which menu items have sold the highest total quantity?
-- Shows the number of completed orders handled by each employee and the total sales associated with those orders.
SELECT
    e.employee_id,
    e.first_name,
    e.last_name,
    COUNT(o.order_id) AS total_orders,
    SUM(o.total) AS total_sales
FROM employees e
JOIN orders o
    ON e.employee_id = o.employee_id
WHERE o.status = 'completed'
GROUP BY e.employee_id, e.first_name, e.last_name
ORDER BY total_sales DESC;