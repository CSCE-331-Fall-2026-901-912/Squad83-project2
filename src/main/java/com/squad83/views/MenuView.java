package com.squad83.views;

import com.squad83.Database;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JavaFX view for managing the menu items of the shop. It displays all menu
 * items in a table and provides a form to add, update, remove (deactivate),
 * clear, and refresh items stored in the database.
 *
 * @author Squad 83
 */
public class MenuView {

	private static TableView<MenuItem> table;
	private static TextField nameField;
	private static TextField priceField;
	private static ComboBox<String> categoryBox;
	private static CheckBox activeBox;

	/**
	 * Builds the menu management screen, including the title, the menu item
	 * table, the input form, and the action buttons. Menu items are loaded
	 * from the database before the view is returned.
	 *
	 * @return the root node of the menu items view
	 */
	public static Parent getView() {

		BorderPane root = new BorderPane();
		root.setPadding(new Insets(15));

		Label title = new Label("Menu Items");
		title.setStyle(
				"-fx-font-size: 24px;" +
				"-fx-font-weight: bold;"
		);

		root.setTop(title);

		table = new TableView<>();

		TableColumn<MenuItem, Number> idColumn = new TableColumn<>("ID");
		idColumn.setCellValueFactory(data -> data.getValue().idProperty());

		TableColumn<MenuItem, String> nameColumn = new TableColumn<>("Item");
		nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

		TableColumn<MenuItem, String> categoryColumn = new TableColumn<>("Category");
		categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());

		TableColumn<MenuItem, String> priceColumn = new TableColumn<>("Price");
		priceColumn.setCellValueFactory(data -> data.getValue().priceProperty());

		TableColumn<MenuItem, Boolean> activeColumn = new TableColumn<>("Active");
		activeColumn.setCellValueFactory(data -> data.getValue().activeProperty().asObject());

		table.getColumns().addAll(
				idColumn,
				nameColumn,
				categoryColumn,
				priceColumn,
				activeColumn
		);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
			if (newItem != null) {
				nameField.setText(newItem.getName());
				priceField.setText(newItem.getPrice());
				categoryBox.setValue(newItem.getCategory());
				activeBox.setSelected(newItem.isActive());
			}
		});

		nameField = new TextField();
		nameField.setPromptText("Item name");

		priceField = new TextField();
		priceField.setPromptText("Price");

		categoryBox = new ComboBox<>();
		categoryBox.getItems().addAll(
				"Milk Tea",
				"Fruit Tea",
				"Matcha",
				"Coffee",
				"Lemonade",
				"Specialty"
		);

		activeBox = new CheckBox("Active");
		activeBox.setSelected(true);

		Button addButton = new Button("Add Item");
		Button updateButton = new Button("Update Item");
		Button removeButton = new Button("Remove Item");
		Button clearButton = new Button("Clear");
		Button refreshButton = new Button("Refresh");

		addButton.setOnAction(e -> addItem());
		updateButton.setOnAction(e -> updateItem());
		removeButton.setOnAction(e -> removeItem());
		clearButton.setOnAction(e -> clearForm());
		refreshButton.setOnAction(e -> loadItems());

		HBox buttons = new HBox(
				10,
				addButton,
				updateButton,
				removeButton,
				clearButton,
				refreshButton
		);

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);
		form.setPadding(new Insets(15, 0, 0, 0));

		form.add(new Label("Name:"), 0, 0);
		form.add(nameField, 1, 0);

		form.add(new Label("Category:"), 2, 0);
		form.add(categoryBox, 3, 0);

		form.add(new Label("Price:"), 0, 1);
		form.add(priceField, 1, 1);

		form.add(activeBox, 2, 1);

		form.add(buttons, 0, 2, 4, 1);

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
	 * Loads all menu items from the database, ordered by item ID, and places
	 * them in the table. If a database error occurs, an error dialog is shown
	 * instead of propagating the exception.
	 *
	 * @throws SQLException handled internally; a failed query or connection is
	 *                      reported to the user through an error dialog
	 */
	private static void loadItems() {

		ObservableList<MenuItem> items = FXCollections.observableArrayList();

		String sql =
				"SELECT menu_item_id, name, category, base_price, is_active " +
				"FROM menu_items " +
				"ORDER BY menu_item_id";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()
		) {
			while (rs.next()) {
				items.add(
						new MenuItem(
								rs.getInt("menu_item_id"),
								rs.getString("name"),
								rs.getString("category"),
								rs.getBigDecimal("base_price").toString(),
								rs.getBoolean("is_active")
						)
				);
			}

			table.setItems(items);

		} catch (SQLException e) {
			showError("Could not load menu items.", e.getMessage());
		}
	}

	/**
	 * Validates the form and inserts a new menu item into the database using
	 * the values currently entered. On success the table is refreshed and the
	 * form is cleared.
	 *
	 * @throws SQLException          handled internally; a database failure is
	 *                               reported through an error dialog
	 * @throws NumberFormatException handled internally; an unparsable price is
	 *                               reported through an error dialog
	 */
	private static void addItem() {

		if (!validateForm()) {
			return;
		}

		String sql =
				"INSERT INTO menu_items " +
				"(name, category, base_price, is_active) " +
				"VALUES (?, ?::menu_category, ?, ?)";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)
		) {
			stmt.setString(1, nameField.getText());
			stmt.setString(2, categoryBox.getValue());
			stmt.setBigDecimal(3, new BigDecimal(priceField.getText()));
			stmt.setBoolean(4, activeBox.isSelected());

			stmt.executeUpdate();

			loadItems();
			clearForm();

		} catch (SQLException | NumberFormatException e) {
			showError("Could not add menu item.", e.getMessage());
		}
	}

	/**
	 * Updates the menu item selected in the table with the values currently in
	 * the form. An error is shown if no item is selected or the form is
	 * invalid. On success the table is refreshed and the form is cleared.
	 *
	 * @throws SQLException          handled internally; a database failure is
	 *                               reported through an error dialog
	 * @throws NumberFormatException handled internally; an unparsable price is
	 *                               reported through an error dialog
	 */
	private static void updateItem() {

		MenuItem selected = table.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No item selected.", "Select a menu item from the table first.");
			return;
		}

		if (!validateForm()) {
			return;
		}

		String sql =
				"UPDATE menu_items " +
				"SET name = ?, " +
				"category = ?::menu_category, " +
				"base_price = ?, " +
				"is_active = ? " +
				"WHERE menu_item_id = ?";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)
		) {
			stmt.setString(1, nameField.getText());
			stmt.setString(2, categoryBox.getValue());
			stmt.setBigDecimal(3, new BigDecimal(priceField.getText()));
			stmt.setBoolean(4, activeBox.isSelected());
			stmt.setInt(5, selected.getId());

			stmt.executeUpdate();

			loadItems();
			clearForm();

		} catch (SQLException | NumberFormatException e) {
			showError("Could not update menu item.", e.getMessage());
		}
	}

	/**
	 * Removes the selected menu item after the user confirms. The item is not
	 * deleted; it is marked inactive in the database. An error is shown if no
	 * item is selected.
	 *
	 * @throws SQLException handled internally; a database failure is reported
	 *                      through an error dialog
	 */
	private static void removeItem() {

		MenuItem selected = table.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No item selected.", "Select a menu item first.");
			return;
		}

		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Remove Menu Item");
		confirmation.setHeaderText("Remove " + selected.getName() + "?");
		confirmation.setContentText("The item will be marked inactive.");

		ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);

		if (result != ButtonType.OK) {
			return;
		}

		String sql =
				"UPDATE menu_items " +
				"SET is_active = false " +
				"WHERE menu_item_id = ?";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)
		) {
			stmt.setInt(1, selected.getId());

			stmt.executeUpdate();

			loadItems();
			clearForm();

		} catch (SQLException e) {
			showError("Could not remove menu item.", e.getMessage());
		}
	}

	/**
	 * Checks that the form fields hold valid input: the name must not be
	 * empty, a category must be chosen, and the price must be a non-negative
	 * number. An error dialog is shown for the first problem found.
	 *
	 * @return {@code true} if all form fields are valid, {@code false} otherwise
	 * @throws NumberFormatException handled internally; an unparsable price is
	 *                               reported through an error dialog
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

		try {
			BigDecimal price = new BigDecimal(priceField.getText());

			if (price.compareTo(BigDecimal.ZERO) < 0) {
				showError("Invalid price.", "Price cannot be negative.");
				return false;
			}

		} catch (NumberFormatException e) {
			showError("Invalid price.", "Enter a valid number such as 5.99.");
			return false;
		}

		return true;
	}

	/**
	 * Resets the form to its default state by clearing the name and price
	 * fields, removing the category selection, checking the active box, and
	 * clearing the table selection.
	 */
	private static void clearForm() {

		nameField.clear();
		priceField.clear();
		categoryBox.setValue(null);
		activeBox.setSelected(true);

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
	 * Model class representing a single row of the menu items table. Fields
	 * are stored as JavaFX properties so the table can bind to them.
	 *
	 * @author Squad 83
	 */
	public static class MenuItem {

		private final SimpleIntegerProperty id;
		private final SimpleStringProperty name;
		private final SimpleStringProperty category;
		private final SimpleStringProperty price;
		private final SimpleBooleanProperty active;

		/**
		 * Creates a menu item with the given values.
		 *
		 * @param id       the unique menu item ID
		 * @param name     the display name of the item
		 * @param category the menu category of the item
		 * @param price    the base price of the item, as text
		 * @param active   whether the item is currently active on the menu
		 */
		public MenuItem(int id, String name, String category, String price, boolean active) {

			this.id = new SimpleIntegerProperty(id);
			this.name = new SimpleStringProperty(name);
			this.category = new SimpleStringProperty(category);
			this.price = new SimpleStringProperty(price);
			this.active = new SimpleBooleanProperty(active);
		}

		/**
		 * Gets the menu item ID.
		 *
		 * @return the menu item ID
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
		 * Gets the item price.
		 *
		 * @return the item price, as text
		 */
		public String getPrice() {
			return price.get();
		}

		/**
		 * Gets the price property for table binding.
		 *
		 * @return the price property
		 */
		public SimpleStringProperty priceProperty() {
			return price;
		}

		/**
		 * Checks whether the item is active on the menu.
		 *
		 * @return {@code true} if the item is active, {@code false} otherwise
		 */
		public boolean isActive() {
			return active.get();
		}

		/**
		 * Gets the active property for table binding.
		 *
		 * @return the active property
		 */
		public SimpleBooleanProperty activeProperty() {
			return active;
		}
	}
}