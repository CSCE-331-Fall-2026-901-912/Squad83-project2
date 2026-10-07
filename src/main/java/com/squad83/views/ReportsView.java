package com.squad83.views;

import com.squad83.Database;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

public class ReportsView {

    private static TableView<ObservableList<String>> table;
    private static ComboBox<String> reportBox;
    private static Label resultLabel;

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
                "Daily Sales",
                "Best Selling Items",
                "Employee Sales",
                "Peak Sales Hours",
                "Sales by Payment Method",
                "Inventory Reorder"
        );

        reportBox.setPromptText("Select a report");

        Button generateButton = new Button("Generate Report");
        Button clearButton = new Button("Clear");

        generateButton.setOnAction(e -> generateReport());
        clearButton.setOnAction(e -> clearReport());

        HBox controls = new HBox(
                10,
                new Label("Report:"),
                reportBox,
                generateButton,
                clearButton
        );

        table = new TableView<>();

        resultLabel = new Label("Select a report to begin.");

        VBox content = new VBox(
                15,
                controls,
                table,
                resultLabel
        );

        VBox.setVgrow(table, Priority.ALWAYS);

        root.setCenter(content);

        return root;
    }

    private static void generateReport() {

        String selectedReport = reportBox.getValue();

        if (selectedReport == null) {
            showError(
                    "No report selected.",
                    "Please select a report before generating."
            );

            return;
        }

        String sql;

        switch (selectedReport) {

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

            default:
                showError(
                        "Invalid report.",
                        "The selected report could not be generated."
                );

                return;
        }

        loadReport(sql, selectedReport);
    }

    private static void loadReport(String sql, String reportName) {

        ObservableList<ObservableList<String>> rows =
                FXCollections.observableArrayList();

        table.getColumns().clear();
        table.getItems().clear();

        try (
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {

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

            table.setItems(rows);

            resultLabel.setText(
                    reportName + " - " +
                    rows.size() +
                    " result(s)"
            );

        } catch (SQLException e) {

            showError(
                    "Could not generate report.",
                    e.getMessage()
            );
        }
    }

    private static void clearReport() {

        reportBox.setValue(null);

        table.getColumns().clear();
        table.getItems().clear();

        resultLabel.setText("Select a report to begin.");
    }

    private static void showError(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
