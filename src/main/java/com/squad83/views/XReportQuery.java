package com.squad83.views;

public class XReportQuery {

	public static String getSql() {

		// latest_day skips days with fewer than 50 completed orders, such as single test orders
		return
				"WITH latest_day AS (" +
				"SELECT DATE_TRUNC('day', order_time) AS day_start " +
				"FROM orders " +
				"WHERE status = 'completed' " +
				"GROUP BY DATE_TRUNC('day', order_time) " +
				"HAVING COUNT(*) >= 50 " +
				"ORDER BY day_start DESC " +
				"LIMIT 1" +
				") " +
				"SELECT " +
				"l.day_start::date AS report_date, " +
				"COALESCE(LPAD(EXTRACT(HOUR FROM o.order_time)::int::text, 2, '0') || ':00', 'TOTAL') AS hour, " +
				"COUNT(*) AS total_orders, " +
				"ROUND(SUM(o.total)::numeric, 2) AS total_sales " +
				"FROM orders o " +
				"JOIN latest_day l " +
				"ON o.order_time >= l.day_start " +
				"AND o.order_time < l.day_start + INTERVAL '1 day' " +
				"WHERE o.status = 'completed' " +
				"GROUP BY l.day_start, ROLLUP (EXTRACT(HOUR FROM o.order_time)::int) " +
				"ORDER BY EXTRACT(HOUR FROM o.order_time)::int NULLS LAST";
	}
}
