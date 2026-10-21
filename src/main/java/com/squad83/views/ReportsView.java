package com.squad83.views;

import com.squad83.Database;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Provides the JavaFX manager interface for creating and viewing operational
 * reports from the Squad 83 point-of-sale PostgreSQL database.
 *
 * <p>Managers select a report from a drop-down menu, optionally choose a date
 * range, and view query results in a table whose columns are generated from
 * the result-set metadata. The available reports are:</p>
 * <ul>
 *   <li><b>Sales by Item:</b> quantities sold and sales totals for each menu item
 *       over a selected date range, including only completed orders.</li>
 *   <li><b>Daily Sales:</b> number of completed orders and total sales by day.</li>
 *   <li><b>Best Selling Items:</b> menu items ranked by quantities sold.</li>
 *   <li><b>Employee Sales:</b> completed order counts and sales by employee.</li>
 *   <li><b>Peak Sales Hours:</b> completed order counts and sales grouped by hour.</li>
 *   <li><b>Sales by Payment Method:</b> completed orders and sales by payment type.</li>
 *   <li><b>Inventory Reorder:</b> inventory items at or below their reorder levels.</li>
 *   <li><b>Product Usage Chart:</b> estimated ingredient usage for completed,
 *       top-level order items within a selected date range.</li>
 *   <li><b>X-Report:</b> hourly order and sales totals for the latest qualifying
 *       day, with an overall total, using {@link XReportQuery#getSql()}.</li>
 *   <li><b>Audit Report:</b> counts and monetary totals for completed sales,
 *       voids, refunds, and discards within a selected date range.</li>
 * </ul>
 *
 * <p>This class uses static controls and helper methods to manage the report
 * selection, query execution, display, and error dialogs.</p>
 *
 * @see com.squad83.Database
 * @see XReportQuery
 */
public class ReportsView {

    /** Table displaying the columns and rows returned by the selected report. */
	private static TableView<ObservableList<String>> table;

    /** Drop-down control used to select which management report to generate. */
	private static ComboBox<String> reportBox;

    /** Inclusive start date for reports that support a selected time window. */
	private static DatePicker startDatePicker;

    /** Inclusive end date for reports that support a selected time window. */
	private static DatePicker endDatePicker;

    /** Label displaying instructions or the latest report's result summary. */
	private static Label resultLabel;

    /**
     * Creates the manager reporting view and initializes its JavaFX controls.
     *
     * <p>The view includes a report selector, start and end date pickers, buttons
     * for generating and clearing a report, a results table, and a status label.
     * Date pickers begin with a 30-day range and are enabled only for reports
     * that support date filtering.</p>
     *
     * @return the root {@link Parent} containing the reporting interface
     */
	public static Parent getView() {

		BorderPane root = new BorderPane();
		root.setPadding(new Insets(15));

		Label title = new Label("Manager Reports");
		title.setStyle(
				"-fx-font-size: 24px;" +
				"-fx-font-weight: bold;"
		);

		root.setTop(title);

		reportBox = new ComboBox<>();

		reportBox.getItems().addAll(
        "Sales by Item",
        "Daily Sales",
        "Best Selling Items",
        "Employee Sales",
        "Peak Sales Hours",
        "Sales by Payment Method",
        "Inventory Reorder",
        "Product Usage Chart",
        "X-Report",
        "Audit Report"
);

		reportBox.setPromptText("Select a report");

		// Time window for reports that use one, defaults to the last 30 days.
		startDatePicker = new DatePicker(LocalDate.now().minusDays(30));
		startDatePicker.setPrefWidth(130);
		startDatePicker.setDisable(true);

		endDatePicker = new DatePicker(LocalDate.now());
		endDatePicker.setPrefWidth(130);
		endDatePicker.setDisable(true);

		reportBox.valueProperty().addListener((obs, oldReport, newReport) -> {

			boolean usesDates = usesTimeWindow(newReport);

			startDatePicker.setDisable(!usesDates);
			endDatePicker.setDisable(!usesDates);
		});

		Button generateButton = new Button("Generate Report");
		Button clearButton = new Button("Clear");

		generateButton.setOnAction(e -> generateReport());
		clearButton.setOnAction(e -> clearReport());

		HBox controls = new HBox(
				10,
				new Label("Report:"),
				reportBox,
				new Label("From:"),
				startDatePicker,
				new Label("To:"),
				endDatePicker,
				generateButton,
				clearButton
		);

		controls.setAlignment(Pos.CENTER_LEFT);

		table = new TableView<>();

		resultLabel = new Label("Select a report to begin.");

		VBox content = new VBox(
				15,
				controls,
				table,
				resultLabel
		);

		content.setPadding(new Insets(15, 0, 0, 0));

		VBox.setVgrow(table, Priority.ALWAYS);

		root.setCenter(content);

		return root;
	}

    /**
     * Determines whether a selected report accepts a start and end date.
     *
     * <p>Only Sales by Item, Product Usage Chart, and Audit Report use the
     * date pickers. Other reports use their own SQL-defined time scope.</p>
     *
     * @param report the selected report name; may be {@code null}
     * @return {@code true} when the report accepts a custom date range;
     *         {@code false} otherwise
     */
	private static boolean usesTimeWindow(String report) {

    return "Sales by Item".equals(report) ||
            "Product Usage Chart".equals(report) ||
            "Audit Report".equals(report);
}

    /**
     * Builds the SQL statement for the selected report and requests its results.
     *
     * <p>The report selector determines which SQL query to use. Queries for
     * completed-order reports filter by order status. For Sales by Item,
     * Product Usage Chart, and Audit Report, a valid time window is required;
     * {@link #loadReport(String, String)} supplies its timestamp parameters.
     * The X-Report SQL is supplied by {@link XReportQuery#getSql()}.</p>
     *
     * <p>If no report is selected, the date range is invalid, or the selected
     * report is unrecognized, an error dialog is displayed instead.</p>
     *
     * <p>The Audit Report groups the four supported transaction statuses
     * ({@code completed}, {@code voided}, {@code refunded}, and {@code discard})
     * and uses a left join so categories with no matching orders still appear
     * with zero totals.</p>
     */
	private static void generateReport() {

		String selectedReport = reportBox.getValue();

		if (selectedReport == null) {
			showError("No report selected.", "Please select a report before generating.");
			return;
		}

		if (usesTimeWindow(selectedReport) && !validateDates()) {
			return;
		}

		String sql;

		switch (selectedReport) {

			case "Sales by Item":
				// End date is inclusive, so the window runs up to the start of the next day.
				sql =
						"SELECT " +
						"m.name AS menu_item, " +
						"m.category, " +
						"SUM(oi.quantity) AS quantity_sold, " +
						"ROUND(SUM(oi.quantity * oi.unit_price)::numeric, 2) AS item_sales " +
						"FROM order_items oi " +
						"JOIN menu_items m " +
						"ON oi.menu_item_id = m.menu_item_id " +
						"JOIN orders o " +
						"ON oi.order_id = o.order_id " +
						"WHERE o.status = 'completed' " +
						"AND o.order_time >= ? " +
						"AND o.order_time < ? " +
						"GROUP BY m.menu_item_id, m.name, m.category " +
						"ORDER BY item_sales DESC";
				break;

			case "Daily Sales":
				sql =
						"SELECT " +
						"DATE(order_time) AS sale_date, " +
						"COUNT(*) AS total_orders, " +
						"ROUND(SUM(total)::numeric, 2) AS total_sales " +
						"FROM orders " +
						"WHERE status = 'completed' " +
						"GROUP BY DATE(order_time) " +
						"ORDER BY sale_date DESC";
				break;

			case "Best Selling Items":
				sql =
						"SELECT " +
						"m.name AS menu_item, " +
						"SUM(oi.quantity) AS quantity_sold " +
						"FROM order_items oi " +
						"JOIN menu_items m " +
						"ON oi.menu_item_id = m.menu_item_id " +
						"JOIN orders o " +
						"ON oi.order_id = o.order_id " +
						"WHERE o.status = 'completed' " +
						"GROUP BY m.menu_item_id, m.name " +
						"ORDER BY quantity_sold DESC";
				break;

			case "Employee Sales":
				sql =
						"SELECT " +
						"e.employee_id, " +
						"e.first_name, " +
						"e.last_name, " +
						"COUNT(o.order_id) AS total_orders, " +
						"ROUND(SUM(o.total)::numeric, 2) AS total_sales " +
						"FROM employees e " +
						"JOIN orders o " +
						"ON e.employee_id = o.employee_id " +
						"WHERE o.status = 'completed' " +
						"GROUP BY e.employee_id, e.first_name, e.last_name " +
						"ORDER BY total_sales DESC";
				break;

			case "Peak Sales Hours":
				sql =
						"SELECT " +
						"EXTRACT(HOUR FROM order_time)::int AS hour, " +
						"COUNT(*) AS total_orders, " +
						"ROUND(SUM(total)::numeric, 2) AS total_sales " +
						"FROM orders " +
						"WHERE status = 'completed' " +
						"GROUP BY EXTRACT(HOUR FROM order_time) " +
						"ORDER BY total_sales DESC";
				break;

			case "Sales by Payment Method":
				sql =
						"SELECT " +
						"payment_method, " +
						"COUNT(*) AS total_orders, " +
						"ROUND(SUM(total)::numeric, 2) AS total_sales " +
						"FROM orders " +
						"WHERE status = 'completed' " +
						"GROUP BY payment_method " +
						"ORDER BY total_sales DESC";
				break;

			case "Inventory Reorder":
				sql =
						"SELECT " +
						"inventory_id, " +
						"name, " +
						"quantity_on_hand, " +
						"reorder_threshold, " +
						"unit " +
						"FROM inventory " +
						"WHERE quantity_on_hand <= reorder_threshold " +
						"ORDER BY quantity_on_hand ASC";
				break;

			case "Product Usage Chart":
				sql =
						"SELECT " +
						"i.name AS inventory_item, " +
						"i.category, " +
						"i.unit, " +
						"ROUND(SUM(oi.quantity * mii.quantity_used)::numeric, 2) AS amount_used " +
						"FROM orders o " +
						"JOIN order_items oi " +
						"ON o.order_id = oi.order_id " +
						"JOIN menu_item_ingredients mii " +
						"ON oi.menu_item_id = mii.menu_item_id " +
						"JOIN inventory i " +
						"ON mii.inventory_id = i.inventory_id " +
						"WHERE o.status = 'completed' " +
						"AND o.order_time >= ? " +
						"AND o.order_time < ? " +
						"AND oi.parent_order_item_id IS NULL " +
						"GROUP BY i.inventory_id, i.name, i.category, i.unit " +
						"ORDER BY amount_used DESC";
				break;

			case "X-Report":
    sql = XReportQuery.getSql();
    break;

case "Audit Report":
    sql =
            "SELECT " +
            "types.transaction_type, " +
            "COUNT(o.order_id) AS transaction_count, " +
            "ROUND(COALESCE(SUM(o.total), 0), 2) AS total_amount " +
            "FROM (VALUES " +
            "('completed', 'Completed Sales'), " +
            "('voided', 'Voids'), " +
            "('refunded', 'Returns / Refunds'), " +
            "('discard', 'Discards') " +
            ") AS types(status, transaction_type) " +
            "LEFT JOIN orders o " +
            "ON o.status = types.status " +
            "AND o.order_time >= ? " +
            "AND o.order_time < ? " +
            "GROUP BY types.status, types.transaction_type " +
            "ORDER BY CASE types.status " +
            "WHEN 'completed' THEN 1 " +
            "WHEN 'voided' THEN 2 " +
            "WHEN 'refunded' THEN 3 " +
            "WHEN 'discard' THEN 4 END";
    break;

default:
    showError("Invalid report.", "The selected report could not be generated.");
    return;
		}

		loadReport(sql, selectedReport);
	}

    /**
     * Validates the start and end dates chosen for a time-window report.
     *
     * <p>Both dates must be selected, and the start date must not be after
     * the end date. Any invalid selection displays an explanatory dialog.</p>
     *
     * @return {@code true} when both dates are present and ordered correctly;
     *         {@code false} otherwise
     */
	private static boolean validateDates() {

		LocalDate startDate = startDatePicker.getValue();
		LocalDate endDate = endDatePicker.getValue();

		if (startDate == null || endDate == null) {
			showError("Invalid time window.", "Please select both a start and end date.");
			return false;
		}

		if (startDate.isAfter(endDate)) {
			showError("Invalid time window.", "Start date must be on or before the end date.");
			return false;
		}

		return true;
	}

    /**
     * Executes the selected report query and presents its rows in the table.
     *
     * <p>A JDBC {@link PreparedStatement} binds date parameters when required,
     * using an inclusive start timestamp and an exclusive timestamp at the
     * beginning of the day after the selected end date. The method uses
     * {@link ResultSetMetaData} to create table columns dynamically, converts
     * returned values to strings, and updates the result count/status label.
     * SQL errors are displayed through {@link #showError(String, String)}.</p>
     *
     * @param sql the SQL query for the selected management report
     * @param reportName the report title used to determine date binding and
     *                   describe the displayed results
     */
	private static void loadReport(String sql, String reportName) {

		ObservableList<ObservableList<String>> rows =
				FXCollections.observableArrayList();

		table.getColumns().clear();
		table.getItems().clear();

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)
		) {

			if (usesTimeWindow(reportName)) {
				stmt.setTimestamp(
						1,
						Timestamp.valueOf(
								startDatePicker.getValue().atStartOfDay()
						)
				);

				stmt.setTimestamp(
						2,
						Timestamp.valueOf(
								endDatePicker.getValue().plusDays(1).atStartOfDay()
						)
				);
			}

			try (ResultSet rs = stmt.executeQuery()) {

				ResultSetMetaData metadata = rs.getMetaData();

				int columnCount = metadata.getColumnCount();

				for (int i = 1; i <= columnCount; i++) {

					final int columnIndex = i - 1;

					String columnName = metadata.getColumnLabel(i);

					TableColumn<ObservableList<String>, String> column =
							new TableColumn<>(columnName);

					column.setCellValueFactory(
							data -> new ReadOnlyStringWrapper(
									data.getValue().get(columnIndex)
							)
					);

					table.getColumns().add(column);
				}

				while (rs.next()) {

					ObservableList<String> row =
							FXCollections.observableArrayList();

					for (int i = 1; i <= columnCount; i++) {

						Object value = rs.getObject(i);

						if (value == null) {
							row.add("");
						} else {
							row.add(value.toString());
						}
					}

					rows.add(row);
				}
			}

			table.setItems(rows);

			String summary =
					reportName +
					" - " +
					rows.size() +
					" result(s)";

			if (usesTimeWindow(reportName)) {
				summary +=
						" from " +
						startDatePicker.getValue() +
						" to " +
						endDatePicker.getValue();
			}

			resultLabel.setText(summary);

		} catch (SQLException e) {
			showError("Could not generate report.", e.getMessage());
		}
	}

    /**
     * Resets the reporting screen to its initial state.
     *
     * <p>Clears the selected report and results, restores the default 30-day
     * date range, and replaces the status message with the initial prompt.</p>
     */
	private static void clearReport() {

		reportBox.setValue(null);

		startDatePicker.setValue(LocalDate.now().minusDays(30));
		endDatePicker.setValue(LocalDate.now());

		table.getColumns().clear();
		table.getItems().clear();

		resultLabel.setText("Select a report to begin.");
	}

    /**
     * Displays a modal JavaFX error dialog with the supplied message.
     *
     * @param title the title displayed in the error dialog window
     * @param message the explanation shown to the manager
     */
	private static void showError(String title, String message) {

		Alert alert = new Alert(Alert.AlertType.ERROR);

		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}
}
