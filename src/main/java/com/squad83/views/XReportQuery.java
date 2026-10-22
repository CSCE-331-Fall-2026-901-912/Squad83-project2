package com.squad83.views;

/**
 * Holds the SQL query behind the X report. The report shows the completed
 * orders and sales for each hour of the most recent business day, followed by
 * a total row for the whole day.
 *
 * @author Squad 83
 */
public class XReportQuery {

	/**
	 * Gets the SQL for the X report. The query looks at the most recent day
	 * that has at least 50 completed orders and returns one row per hour with
	 * the order count and sales total, plus a final row labeled {@code TOTAL}
	 * for the whole day. Each row has the columns {@code report_date},
	 * {@code hour}, {@code total_orders}, and {@code total_sales}.
	 *
	 * @return the SQL text of the X report query
	 */
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