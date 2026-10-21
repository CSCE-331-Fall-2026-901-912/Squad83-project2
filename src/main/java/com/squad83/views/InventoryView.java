package com.squad83.views;

import com.squad83.Database;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JavaFX view for managing the shop's inventory. It displays all inventory
 * items in a table along with a status that flags items at or below their
 * reorder threshold, and provides a form to add, update, restock, clear, and
 * refresh items stored in the database.
 *
 * @author Squad 83
 */
public class InventoryView {

	/** Table showing every inventory item. */
	private static TableView<InventoryItem> table;
	/** Name of the inventory item. */
	private static TextField nameField;
	/** Category picker (ingredient, topping, or supply). */
	private static ComboBox<String> categoryBox;
	/** Unit picker (oz, g, or each). */
	private static ComboBox<String> unitBox;
	/** Quantity currently on hand. */
	private static TextField quantityField;
	/** Quantity at or below which the item needs to be reordered. */
	private static TextField thresholdField;
	/** Cost of one unit of the item. */
	private static TextField costField;
	/** Amount to add to the selected item when restocking. */
	private static TextField restockField;

	/**
	 * Builds the inventory management screen, including the title, the
	 * inventory table, the input form, the restock row, and the action
	 * buttons. Selecting a row in the table fills the form with that item's
	 * values. Inventory items are loaded from the database before the view is
	 * returned.
	 *
	 * @return the root node of the inventory view
	 */
	public static Parent getView() {

		BorderPane root = new BorderPane();
		root.setPadding(new Insets(15));

		Label title = new Label("Inventory");
		title.setStyle(
				"-fx-font-size: 24px;" +
				"-fx-font-weight: bold;"
		);

		root.setTop(title);

		table = new TableView<>();

		TableColumn<InventoryItem, Number> idColumn = new TableColumn<>("ID");
		idColumn.setCellValueFactory(data -> data.getValue().idProperty());

		TableColumn<InventoryItem, String> nameColumn = new TableColumn<>("Item");
		nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

		TableColumn<InventoryItem, String> categoryColumn = new TableColumn<>("Category");
		categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());

		TableColumn<InventoryItem, String> unitColumn = new TableColumn<>("Unit");
		unitColumn.setCellValueFactory(data -> data.getValue().unitProperty());

		TableColumn<InventoryItem, Number> quantityColumn = new TableColumn<>("On Hand");
		quantityColumn.setCellValueFactory(data -> data.getValue().quantityProperty());

		TableColumn<InventoryItem, Number> thresholdColumn = new TableColumn<>("Reorder At");
		thresholdColumn.setCellValueFactory(data -> data.getValue().thresholdProperty());

		TableColumn<InventoryItem, Number> costColumn = new TableColumn<>("Unit Cost");
		costColumn.setCellValueFactory(data -> data.getValue().costProperty());

		TableColumn<InventoryItem, String> statusColumn = new TableColumn<>("Status");
		statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

		table.getColumns().addAll(
				idColumn,
				nameColumn,
				categoryColumn,
				unitColumn,
				quantityColumn,
				thresholdColumn,
				costColumn,
				statusColumn
		);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
			if (newItem != null) {
				nameField.setText(newItem.getName());
				categoryBox.setValue(newItem.getCategory());
				unitBox.setValue(newItem.getUnit());
				quantityField.setText(String.valueOf(newItem.getQuantity()));
				thresholdField.setText(String.valueOf(newItem.getThreshold()));
				costField.setText(String.valueOf(newItem.getCost()));
			}
		});

		nameField = new TextField();
		nameField.setPromptText("Item name");

		categoryBox = new ComboBox<>();
		categoryBox.getItems().addAll(
				"ingredient",
				"topping",
				"supply"
		);

		unitBox = new ComboBox<>();
		unitBox.getItems().addAll(
				"oz",
				"g",
				"each"
		);

		quantityField = new TextField();
		quantityField.setPromptText("Quantity on hand");

		thresholdField = new TextField();
		thresholdField.setPromptText("Reorder threshold");

		costField = new TextField();
		costField.setPromptText("Cost per unit");

		restockField = new TextField();
		restockField.setPromptText("Amount to add");

		Button addButton = new Button("Add Item");
		Button updateButton = new Button("Update Item");
		Button restockButton = new Button("Restock");
		Button clearButton = new Button("Clear");
		Button refreshButton = new Button("Refresh");

		addButton.setOnAction(e -> addItem());
		updateButton.setOnAction(e -> updateItem());
		restockButton.setOnAction(e -> restockItem());
		clearButton.setOnAction(e -> clearForm());
		refreshButton.setOnAction(e -> loadItems());

		HBox buttons = new HBox(
				10,
				addButton,
				updateButton,
				clearButton,
				refreshButton
		);

		HBox restockRow = new HBox(
				10,
				restockField,
				restockButton
		);

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);
		form.setPadding(new Insets(15, 0, 0, 0));

		form.add(new Label("Name:"), 0, 0);
		form.add(nameField, 1, 0);

		form.add(new Label("Category:"), 2, 0);
		form.add(categoryBox, 3, 0);

		form.add(new Label("Unit:"), 4, 0);
		form.add(unitBox, 5, 0);

		form.add(new Label("Quantity:"), 0, 1);
		form.add(quantityField, 1, 1);

		form.add(new Label("Reorder At:"), 2, 1);
		form.add(thresholdField, 3, 1);

		form.add(new Label("Unit Cost:"), 4, 1);
		form.add(costField, 5, 1);

		form.add(new Label("Restock:"), 0, 2);
		form.add(restockRow, 1, 2, 3, 1);

		form.add(buttons, 0, 3, 6, 1);

		VBox content = new VBox(
				15,
				table,
				form
		);

		VBox.setVgrow(table, Priority.ALWAYS);

		root.setCenter(content);

		loadItems();

		return root;
	}

	/**
	 * Loads all inventory items from the database, ordered by inventory ID,
	 * and places them in the table. If a database error occurs, an error
	 * dialog is shown instead of propagating the exception.
	 */
	private static void loadItems() {

		ObservableList<InventoryItem> items = FXCollections.observableArrayList();

		String sql =
				"SELECT inventory_id, name, category, unit, " +
				"quantity_on_hand, reorder_threshold, unit_cost " +
				"FROM inventory " +
				"ORDER BY inventory_id";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()
		) {
			while (rs.next()) {
				items.add(
						new InventoryItem(
								rs.getInt("inventory_id"),
								rs.getString("name"),
								rs.getString("category"),
								rs.getString("unit"),
								rs.getDouble("quantity_on_hand"),
								rs.getDouble("reorder_threshold"),
								rs.getDouble("unit_cost")
						)
				);
			}

			table.setItems(items);

		} catch (SQLException e) {
			showError("Could not load inventory.", e.getMessage());
		}
	}

	/**
	 * Validates the form and inserts a new inventory item into the database
	 * using the values currently entered. The new item's ID is one more than
	 * the largest existing ID. On success the table is refreshed and the form
	 * is cleared.
	 */
	private static void addItem() {

		if (!validateForm()) {
			return;
		}

		// inventory_id has no auto-increment, so the next id is computed in the insert
		String sql =
				"INSERT INTO inventory " +
				"(inventory_id, name, category, unit, quantity_on_hand, reorder_threshold, unit_cost) " +
				"SELECT COALESCE(MAX(inventory_id), 0) + 1, ?, ?, ?, ?, ?, ? " +
				"FROM inventory";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)
		) {
			stmt.setString(1, nameField.getText().trim());
			stmt.setString(2, categoryBox.getValue());
			stmt.setString(3, unitBox.getValue());
			stmt.setDouble(4, Double.parseDouble(quantityField.getText()));
			stmt.setDouble(5, Double.parseDouble(thresholdField.getText()));
			stmt.setDouble(6, Double.parseDouble(costField.getText()));

			stmt.executeUpdate();

			loadItems();
			clearForm();

		} catch (SQLException e) {
			showError("Could not add inventory item.", e.getMessage());
		}
	}

	/**
	 * Updates the inventory item selected in the table with the values
	 * currently in the form. An error is shown if no item is selected or the
	 * form is invalid. On success the table is refreshed and the form is
	 * cleared.
	 */
	private static void updateItem() {

		InventoryItem selected = table.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No item selected.", "Select an inventory item from the table first.");
			return;
		}

		if (!validateForm()) {
			return;
		}

		String sql =
				"UPDATE inventory " +
				"SET name = ?, " +
				"category = ?, " +
				"unit = ?, " +
				"quantity_on_hand = ?, " +
				"reorder_threshold = ?, " +
				"unit_cost = ? " +
				"WHERE inventory_id = ?";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)
		) {
			stmt.setString(1, nameField.getText().trim());
			stmt.setString(2, categoryBox.getValue());
			stmt.setString(3, unitBox.getValue());
			stmt.setDouble(4, Double.parseDouble(quantityField.getText()));
			stmt.setDouble(5, Double.parseDouble(thresholdField.getText()));
			stmt.setDouble(6, Double.parseDouble(costField.getText()));
			stmt.setInt(7, selected.getId());

			stmt.executeUpdate();

			loadItems();
			clearForm();

		} catch (SQLException e) {
			showError("Could not update inventory item.", e.getMessage());
		}
	}

	/**
	 * Adds the amount entered in the restock field to the quantity on hand of
	 * the item selected in the table. An error is shown if no item is
	 * selected or the amount is not a number greater than 0. On success the
	 * table is refreshed and the form is cleared.
	 */
	private static void restockItem() {

		InventoryItem selected = table.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No item selected.", "Select an inventory item to restock.");
			return;
		}

		double amount;

		try {
			amount = Double.parseDouble(restockField.getText());

		} catch (NumberFormatException e) {
			showError("Invalid amount.", "Enter a valid number such as 50.");
			return;
		}

		if (amount <= 0) {
			showError("Invalid amount.", "Restock amount must be greater than 0.");
			return;
		}

		String sql =
				"UPDATE inventory " +
				"SET quantity_on_hand = quantity_on_hand + ? " +
				"WHERE inventory_id = ?";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)
		) {
			stmt.setDouble(1, amount);
			stmt.setInt(2, selected.getId());

			stmt.executeUpdate();

			loadItems();
			clearForm();

		} catch (SQLException e) {
			showError("Could not restock inventory item.", e.getMessage());
		}
	}

	/**
	 * Checks that the form fields hold valid input: the name must not be
	 * empty, a category and unit must be chosen, and the quantity, reorder
	 * threshold, and unit cost must be non-negative numbers. An error dialog
	 * is shown for the first problem found.
	 *
	 * @return {@code true} if all form fields are valid, {@code false} otherwise
	 */
	private static boolean validateForm() {

		if (nameField.getText().trim().isEmpty()) {
			showError("Invalid item.", "Item name cannot be empty.");
			return false;
		}

		if (categoryBox.getValue() == null) {
			showError("Invalid category.", "Please select a category.");
			return false;
		}

		if (unitBox.getValue() == null) {
			showError("Invalid unit.", "Please select a unit.");
			return false;
		}

		if (!isNonNegativeNumber(quantityField.getText())) {
			showError("Invalid quantity.", "Quantity must be a number that is 0 or greater.");
			return false;
		}

		if (!isNonNegativeNumber(thresholdField.getText())) {
			showError("Invalid reorder threshold.", "Reorder threshold must be a number that is 0 or greater.");
			return false;
		}

		if (!isNonNegativeNumber(costField.getText())) {
			showError("Invalid unit cost.", "Unit cost must be a number that is 0 or greater, such as 0.25.");
			return false;
		}

		return true;
	}

	/**
	 * Checks whether the given text is a number that is 0 or greater.
	 *
	 * @param text the text to check
	 * @return {@code true} if the text is a non-negative number, {@code false} otherwise
	 */
	private static boolean isNonNegativeNumber(String text) {

		try {
			return Double.parseDouble(text) >= 0;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Resets the form to its default state by clearing every text field,
	 * removing the category and unit selections, and clearing the table
	 * selection.
	 */
	private static void clearForm() {

		nameField.clear();
		categoryBox.setValue(null);
		unitBox.setValue(null);
		quantityField.clear();
		thresholdField.clear();
		costField.clear();
		restockField.clear();

		table.getSelectionModel().clearSelection();
	}

	/**
	 * Displays a modal error dialog and waits for the user to close it.
	 *
	 * @param title   the title of the dialog window
	 * @param message the message shown in the dialog body
	 */
	private static void showError(String title, String message) {

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
	 * Model class representing a single row of the inventory table. Fields
	 * are stored as JavaFX properties so the table can bind to them.
	 *
	 * @author Squad 83
	 */
	public static class InventoryItem {

		/** Unique inventory ID. */
		private final SimpleIntegerProperty id;
		/** Display name of the item. */
		private final SimpleStringProperty name;
		/** Inventory category (ingredient, topping, or supply). */
		private final SimpleStringProperty category;
		/** Unit the item is measured in (oz, g, or each). */
		private final SimpleStringProperty unit;
		/** Quantity currently on hand. */
		private final SimpleDoubleProperty quantity;
		/** Quantity at or below which the item needs to be reordered. */
		private final SimpleDoubleProperty threshold;
		/** Cost of one unit of the item. */
		private final SimpleDoubleProperty cost;

		/**
		 * Creates an inventory item with the given values.
		 *
		 * @param id        the unique inventory ID
		 * @param name      the display name of the item
		 * @param category  the inventory category of the item
		 * @param unit      the unit the item is measured in
		 * @param quantity  the quantity currently on hand
		 * @param threshold the quantity at or below which the item needs reordering
		 * @param cost      the cost of one unit of the item
		 */
		public InventoryItem(int id, String name, String category, String unit, double quantity, double threshold, double cost) {

			this.id = new SimpleIntegerProperty(id);
			this.name = new SimpleStringProperty(name);
			this.category = new SimpleStringProperty(category);
			this.unit = new SimpleStringProperty(unit);
			this.quantity = new SimpleDoubleProperty(quantity);
			this.threshold = new SimpleDoubleProperty(threshold);
			this.cost = new SimpleDoubleProperty(cost);
		}

		/**
		 * Gets the inventory ID.
		 *
		 * @return the inventory ID
		 */
		public int getId() {
			return id.get();
		}

		/**
		 * Gets the ID property for table binding.
		 *
		 * @return the ID property
		 */
		public SimpleIntegerProperty idProperty() {
			return id;
		}

		/**
		 * Gets the item name.
		 *
		 * @return the item name
		 */
		public String getName() {
			return name.get();
		}

		/**
		 * Gets the name property for table binding.
		 *
		 * @return the name property
		 */
		public SimpleStringProperty nameProperty() {
			return name;
		}

		/**
		 * Gets the item category.
		 *
		 * @return the item category
		 */
		public String getCategory() {
			return category.get();
		}

		/**
		 * Gets the category property for table binding.
		 *
		 * @return the category property
		 */
		public SimpleStringProperty categoryProperty() {
			return category;
		}

		/**
		 * Gets the unit the item is measured in.
		 *
		 * @return the item unit
		 */
		public String getUnit() {
			return unit.get();
		}

		/**
		 * Gets the unit property for table binding.
		 *
		 * @return the unit property
		 */
		public SimpleStringProperty unitProperty() {
			return unit;
		}

		/**
		 * Gets the quantity currently on hand.
		 *
		 * @return the quantity on hand
		 */
		public double getQuantity() {
			return quantity.get();
		}

		/**
		 * Gets the quantity property for table binding.
		 *
		 * @return the quantity property
		 */
		public SimpleDoubleProperty quantityProperty() {
			return quantity;
		}

		/**
		 * Gets the reorder threshold.
		 *
		 * @return the quantity at or below which the item needs reordering
		 */
		public double getThreshold() {
			return threshold.get();
		}

		/**
		 * Gets the threshold property for table binding.
		 *
		 * @return the threshold property
		 */
		public SimpleDoubleProperty thresholdProperty() {
			return threshold;
		}

		/**
		 * Gets the cost of one unit of the item.
		 *
		 * @return the unit cost
		 */
		public double getCost() {
			return cost.get();
		}

		/**
		 * Gets the cost property for table binding.
		 *
		 * @return the cost property
		 */
		public SimpleDoubleProperty costProperty() {
			return cost;
		}

		/**
		 * Gets the stock status shown in the table's Status column.
		 *
		 * @return {@code "Reorder"} if the quantity on hand is at or below the
		 *         reorder threshold, {@code "OK"} otherwise
		 */
		public String getStatus() {
			if (getQuantity() <= getThreshold()) {
				return "Reorder";
			}

			return "OK";
		}
	}
}
