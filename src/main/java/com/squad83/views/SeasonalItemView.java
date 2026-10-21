package com.squad83.views;

import com.squad83.Database;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
 * Manager view for creating a new seasonal menu item together with its recipe.
 * <p>
 * The manager enters the item's name, price, and category, then builds the recipe
 * by choosing inventory items and the amount of each used per drink. Creating the
 * item saves it to {@code menu_items} and its recipe to {@code menu_item_ingredients}
 * in one transaction, so the new drink appears on the cashier menu and deducts
 * inventory correctly when sold.
 */
public class SeasonalItemView {

	/** Name of the new menu item. */
	private static TextField nameField;
	/** Price of the new menu item. */
	private static TextField priceField;
	/** Category picker, filled from the menu_category enum. */
	private static ComboBox<String> categoryBox;
	/** Inventory item picker for building the recipe. */
	private static ComboBox<InventoryOption> ingredientBox;
	/** Amount of the selected inventory item used per drink. */
	private static TextField amountField;
	/** Shows the unit (oz, g, or each) of the selected inventory item. */
	private static Label unitLabel;
	/** Table showing the recipe being built. */
	private static TableView<RecipeLine> recipeTable;
	/** The lines of the recipe being built. */
	private static ObservableList<RecipeLine> recipe;

	/**
	 * Builds the seasonal menu item view and loads its categories and inventory items.
	 *
	 * @return the root node of the view, used by App to fill the Seasonal Item tab
	 */
	public static Parent getView() {

		BorderPane root = new BorderPane();
		root.setPadding(new Insets(15));

		Label title = new Label("New Seasonal Menu Item");
		title.setStyle(
				"-fx-font-size: 24px;" +
				"-fx-font-weight: bold;"
		);

		root.setTop(title);

		// item details
		nameField = new TextField();
		nameField.setPromptText("Item name");

		priceField = new TextField();
		priceField.setPromptText("Price");

		categoryBox = new ComboBox<>();
		categoryBox.setPromptText("Select category");

		GridPane detailsForm = new GridPane();
		detailsForm.setHgap(10);
		detailsForm.setVgap(10);

		detailsForm.add(new Label("Name:"), 0, 0);
		detailsForm.add(nameField, 1, 0);

		detailsForm.add(new Label("Price:"), 2, 0);
		detailsForm.add(priceField, 3, 0);

		detailsForm.add(new Label("Category:"), 4, 0);
		detailsForm.add(categoryBox, 5, 0);

		// recipe builder
		Label recipeTitle = new Label("Recipe (amount per drink)");
		recipeTitle.setStyle(
				"-fx-font-size: 18px;" +
				"-fx-font-weight: bold;"
		);

		ingredientBox = new ComboBox<>();
		ingredientBox.setPromptText("Select inventory item");
		ingredientBox.setPrefWidth(250);

		amountField = new TextField();
		amountField.setPromptText("Amount");
		amountField.setPrefWidth(90);

		unitLabel = new Label("");
		unitLabel.setMinWidth(40);

		ingredientBox.valueProperty().addListener((obs, oldItem, newItem) -> {
			if (newItem != null) {
				unitLabel.setText(newItem.getUnit());
			} else {
				unitLabel.setText("");
			}
		});

		Button addIngredientButton = new Button("Add Ingredient");
		Button removeIngredientButton = new Button("Remove Selected");

		addIngredientButton.setOnAction(e -> addIngredient());
		removeIngredientButton.setOnAction(e -> removeIngredient());

		HBox ingredientRow = new HBox(
				10,
				ingredientBox,
				amountField,
				unitLabel,
				addIngredientButton,
				removeIngredientButton
		);

		ingredientRow.setAlignment(Pos.CENTER_LEFT);

		recipe = FXCollections.observableArrayList();

		recipeTable = new TableView<>(recipe);
		recipeTable.setPlaceholder(new Label("No ingredients yet"));

		TableColumn<RecipeLine, String> itemColumn = new TableColumn<>("Inventory Item");
		itemColumn.setCellValueFactory(data -> data.getValue().nameProperty());
		itemColumn.setPrefWidth(250);

		TableColumn<RecipeLine, String> categoryColumn = new TableColumn<>("Category");
		categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
		categoryColumn.setPrefWidth(120);

		TableColumn<RecipeLine, String> amountColumn = new TableColumn<>("Amount");
		amountColumn.setCellValueFactory(data -> data.getValue().amountTextProperty());
		amountColumn.setPrefWidth(120);

		recipeTable.getColumns().addAll(
				itemColumn,
				categoryColumn,
				amountColumn
		);

		Button clearButton = new Button("Clear");
		Button createButton = new Button("Create Item");
		createButton.setPrefWidth(150);
		createButton.setStyle("-fx-font-weight: bold;");

		clearButton.setOnAction(e -> clearForm());
		createButton.setOnAction(e -> createItem());

		HBox bottomButtons = new HBox(
				10,
				clearButton,
				createButton
		);

		bottomButtons.setAlignment(Pos.CENTER_RIGHT);

		VBox content = new VBox(
				15,
				detailsForm,
				recipeTitle,
				ingredientRow,
				recipeTable,
				bottomButtons
		);

		content.setPadding(new Insets(15, 0, 0, 0));

		VBox.setVgrow(recipeTable, Priority.ALWAYS);

		root.setCenter(content);

		loadCategories();
		loadInventory();

		return root;
	}

	/**
	 * Loads every value of the {@code menu_category} enum into the category drop-down.
	 * Reading the enum from the database means a newly added category, such as
	 * Seasonal, appears without changing this code.
	 */
	private static void loadCategories() {

		ObservableList<String> categories = FXCollections.observableArrayList();

		// reads the menu_category enum so a new value like 'Seasonal' shows up automatically
		String sql = "SELECT unnest(enum_range(NULL::menu_category))::text AS category";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()
		) {
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}

			categoryBox.setItems(categories);

		} catch (SQLException e) {
			showError("Could not load categories.", e.getMessage());
		}
	}

	/**
	 * Loads all inventory items, sorted by category and name, into the ingredient
	 * drop-down used to build the recipe.
	 */
	private static void loadInventory() {

		ObservableList<InventoryOption> items = FXCollections.observableArrayList();

		String sql =
				"SELECT inventory_id, name, category, unit " +
				"FROM inventory " +
				"ORDER BY category, name";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()
		) {
			while (rs.next()) {
				items.add(
						new InventoryOption(
								rs.getInt("inventory_id"),
								rs.getString("name"),
								rs.getString("category"),
								rs.getString("unit")
						)
				);
			}

			ingredientBox.setItems(items);

		} catch (SQLException e) {
			showError("Could not load inventory.", e.getMessage());
		}
	}

	/**
	 * Adds the selected inventory item and amount to the recipe. If the item is
	 * already in the recipe, its amount is replaced instead of adding a duplicate line.
	 * Shows an error if no item is selected or the amount is not a positive number.
	 */
	private static void addIngredient() {

		InventoryOption item = ingredientBox.getValue();

		if (item == null) {
			showError("No ingredient selected.", "Select an inventory item first.");
			return;
		}

		double amount;

		try {
			amount = Double.parseDouble(amountField.getText().trim());

		} catch (NumberFormatException e) {
			showError("Invalid amount.", "Enter a number such as 8 or 1.5.");
			return;
		}

		if (amount <= 0) {
			showError("Invalid amount.", "Amount must be greater than 0.");
			return;
		}

		// the same item twice just updates its amount
		for (RecipeLine line : recipe) {
			if (line.getInventoryId() == item.getId()) {
				line.setAmount(amount);
				recipeTable.refresh();
				clearIngredientInput();
				return;
			}
		}

		recipe.add(new RecipeLine(item, amount));

		clearIngredientInput();
	}

	/**
	 * Removes the selected line from the recipe, or shows an error if none is selected.
	 */
	private static void removeIngredient() {

		RecipeLine selected = recipeTable.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No ingredient selected.", "Select an ingredient in the recipe first.");
			return;
		}

		recipe.remove(selected);
	}

	/**
	 * Validates the form, asks for confirmation, and saves the new menu item and its
	 * recipe in one transaction. Because {@code menu_item_id} has no auto-increment,
	 * the menu_items table is locked while the next ID is chosen so two managers
	 * cannot take the same ID. If any step fails, the whole transaction is rolled back.
	 */
	private static void createItem() {

		if (!validateForm()) {
			return;
		}

		String name = nameField.getText().trim();
		BigDecimal price = new BigDecimal(priceField.getText().trim());

		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Create Menu Item");
		confirmation.setHeaderText("Create " + name + " for $" + price + "?");
		confirmation.setContentText("It will use " + recipe.size() + " inventory item(s) and appear on the cashier menu.");

		ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);

		if (result != ButtonType.OK) {
			return;
		}

		// menu_item_id has no auto-increment, so the lock stops two managers from taking the same id
		String lockSql = "LOCK TABLE menu_items IN EXCLUSIVE MODE";

		String nextIdSql = "SELECT COALESCE(MAX(menu_item_id), 0) + 1 AS next_id FROM menu_items";

		String itemSql =
				"INSERT INTO menu_items " +
				"(menu_item_id, name, base_price, category, is_active) " +
				"VALUES (?, ?, ?, ?::menu_category, true)";

		String recipeSql =
				"INSERT INTO menu_item_ingredients " +
				"(menu_item_id, inventory_id, quantity_used) " +
				"VALUES (?, ?, ?)";

		int newId;

		try (Connection conn = Database.getConnection()) {

			conn.setAutoCommit(false);

			try (
					PreparedStatement lockStmt = conn.prepareStatement(lockSql);
					PreparedStatement nextIdStmt = conn.prepareStatement(nextIdSql);
					PreparedStatement itemStmt = conn.prepareStatement(itemSql);
					PreparedStatement recipeStmt = conn.prepareStatement(recipeSql)
			) {
				lockStmt.execute();

				try (ResultSet rs = nextIdStmt.executeQuery()) {
					rs.next();
					newId = rs.getInt("next_id");
				}

				itemStmt.setInt(1, newId);
				itemStmt.setString(2, name);
				itemStmt.setBigDecimal(3, price);
				itemStmt.setString(4, categoryBox.getValue());
				itemStmt.executeUpdate();

				for (RecipeLine line : recipe) {
					recipeStmt.setInt(1, newId);
					recipeStmt.setInt(2, line.getInventoryId());
					recipeStmt.setDouble(3, line.getAmount());
					recipeStmt.addBatch();
				}

				recipeStmt.executeBatch();

				conn.commit();

			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}

		} catch (SQLException e) {
			showError("Could not create menu item.", e.getMessage());
			return;
		}

		Alert success = new Alert(Alert.AlertType.INFORMATION);
		success.setTitle("Item Created");
		success.setHeaderText(null);
		success.setContentText(name + " was added to the menu as item #" + newId + ".");
		success.showAndWait();

		clearForm();
	}

	/**
	 * Checks that the name is filled in, the price is a non-negative number, a
	 * category is selected, and the recipe has at least one ingredient. Shows an
	 * error describing the first problem found.
	 *
	 * @return true if the form is valid, false otherwise
	 */
	private static boolean validateForm() {

		if (nameField.getText().trim().isEmpty()) {
			showError("Invalid name.", "Item name cannot be empty.");
			return false;
		}

		try {
			BigDecimal price = new BigDecimal(priceField.getText().trim());

			if (price.compareTo(BigDecimal.ZERO) < 0) {
				showError("Invalid price.", "Price cannot be negative.");
				return false;
			}

		} catch (NumberFormatException e) {
			showError("Invalid price.", "Enter a valid number such as 6.75.");
			return false;
		}

		if (categoryBox.getValue() == null) {
			showError("Invalid category.", "Please select a category.");
			return false;
		}

		if (recipe.isEmpty()) {
			showError("Empty recipe.", "Add at least one ingredient to the recipe.");
			return false;
		}

		return true;
	}

	/**
	 * Clears the ingredient drop-down and amount field after an ingredient is added.
	 */
	private static void clearIngredientInput() {

		ingredientBox.setValue(null);
		amountField.clear();
	}

	/**
	 * Resets the whole form, including the item details and the recipe.
	 */
	private static void clearForm() {

		nameField.clear();
		priceField.clear();
		categoryBox.setValue(null);
		recipe.clear();

		clearIngredientInput();
	}

	/**
	 * Shows an error dialog to the user.
	 *
	 * @param title the dialog's window title
	 * @param message the explanation shown in the dialog
	 */
	private static void showError(String title, String message) {

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
	 * An inventory item shown in the ingredient drop-down.
	 */
	public static class InventoryOption {

		/** The item's inventory_id. */
		private final int id;
		/** The item's name. */
		private final String name;
		/** The item's category: ingredient, topping, or supply. */
		private final String category;
		/** The item's unit of measure: oz, g, or each. */
		private final String unit;

		/**
		 * Creates an inventory option.
		 *
		 * @param id the item's inventory_id
		 * @param name the item's name
		 * @param category the item's category: ingredient, topping, or supply
		 * @param unit the item's unit of measure: oz, g, or each
		 */
		public InventoryOption(int id, String name, String category, String unit) {

			this.id = id;
			this.name = name;
			this.category = category;
			this.unit = unit;
		}

		/**
		 * Returns the item's inventory_id.
		 *
		 * @return the item's inventory_id
		 */
		public int getId() {
			return id;
		}

		/**
		 * Returns the item's name.
		 *
		 * @return the item's name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the item's category.
		 *
		 * @return the item's category
		 */
		public String getCategory() {
			return category;
		}

		/**
		 * Returns the item's unit of measure.
		 *
		 * @return the item's unit of measure
		 */
		public String getUnit() {
			return unit;
		}

		// shown in the ingredient drop-down
		/**
		 * Returns the text shown in the drop-down, the name followed by the category.
		 *
		 * @return the text shown in the drop-down, the name followed by the category
		 */
		@Override
		public String toString() {
			return name + " (" + category + ")";
		}
	}

	/**
	 * One line of the recipe being built: an inventory item and the amount of it
	 * used per drink.
	 */
	public static class RecipeLine {

		/** The inventory_id of the item used. */
		private final int inventoryId;
		/** The unit of measure of the item used. */
		private final String unit;
		/** The amount used per drink. */
		private double amount;
		/** The item's name, shown in the Inventory Item column. */
		private final SimpleStringProperty name;
		/** The item's category, shown in the Category column. */
		private final SimpleStringProperty category;
		/** The formatted amount and unit, shown in the Amount column. */
		private final SimpleStringProperty amountText;

		/**
		 * Creates a recipe line.
		 *
		 * @param item the inventory item used
		 * @param amount the amount used per drink, in the item's unit
		 */
		public RecipeLine(InventoryOption item, double amount) {

			this.inventoryId = item.getId();
			this.unit = item.getUnit();
			this.amount = amount;
			this.name = new SimpleStringProperty(item.getName());
			this.category = new SimpleStringProperty(item.getCategory());
			this.amountText = new SimpleStringProperty(formatAmount());
		}

		/**
		 * Formats the amount with its unit for display, without trailing zeros.
		 *
		 * @return the amount and unit, for example "1.5 oz"
		 */
		private String formatAmount() {
			return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString() + " " + unit;
		}

		/**
		 * Returns the inventory_id of the item used.
		 *
		 * @return the inventory_id of the item used
		 */
		public int getInventoryId() {
			return inventoryId;
		}

		/**
		 * Returns the amount used per drink.
		 *
		 * @return the amount used per drink
		 */
		public double getAmount() {
			return amount;
		}

		/**
		 * Changes the amount used and updates the displayed text.
		 *
		 * @param newAmount the new amount used per drink
		 */
		public void setAmount(double newAmount) {
			amount = newAmount;
			amountText.set(formatAmount());
		}

		/**
		 * Returns the item name, bound to the Inventory Item column.
		 *
		 * @return the item name, bound to the Inventory Item column
		 */
		public SimpleStringProperty nameProperty() {
			return name;
		}

		/**
		 * Returns the item category, bound to the Category column.
		 *
		 * @return the item category, bound to the Category column
		 */
		public SimpleStringProperty categoryProperty() {
			return category;
		}

		/**
		 * Returns the formatted amount, bound to the Amount column.
		 *
		 * @return the formatted amount, bound to the Amount column
		 */
		public SimpleStringProperty amountTextProperty() {
			return amountText;
		}
	}
}