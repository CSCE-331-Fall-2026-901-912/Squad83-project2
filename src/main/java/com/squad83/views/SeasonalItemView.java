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

public class SeasonalItemView {

	private static TextField nameField;
	private static TextField priceField;
	private static ComboBox<String> categoryBox;
	private static ComboBox<InventoryOption> ingredientBox;
	private static TextField amountField;
	private static Label unitLabel;
	private static TableView<RecipeLine> recipeTable;
	private static ObservableList<RecipeLine> recipe;

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

	private static void removeIngredient() {

		RecipeLine selected = recipeTable.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No ingredient selected.", "Select an ingredient in the recipe first.");
			return;
		}

		recipe.remove(selected);
	}

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

	private static void clearIngredientInput() {

		ingredientBox.setValue(null);
		amountField.clear();
	}

	private static void clearForm() {

		nameField.clear();
		priceField.clear();
		categoryBox.setValue(null);
		recipe.clear();

		clearIngredientInput();
	}

	private static void showError(String title, String message) {

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static class InventoryOption {

		private final int id;
		private final String name;
		private final String category;
		private final String unit;

		public InventoryOption(int id, String name, String category, String unit) {

			this.id = id;
			this.name = name;
			this.category = category;
			this.unit = unit;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getCategory() {
			return category;
		}

		public String getUnit() {
			return unit;
		}

		// shown in the ingredient drop-down
		@Override
		public String toString() {
			return name + " (" + category + ")";
		}
	}

	public static class RecipeLine {

		private final int inventoryId;
		private final String unit;
		private double amount;
		private final SimpleStringProperty name;
		private final SimpleStringProperty category;
		private final SimpleStringProperty amountText;

		public RecipeLine(InventoryOption item, double amount) {

			this.inventoryId = item.getId();
			this.unit = item.getUnit();
			this.amount = amount;
			this.name = new SimpleStringProperty(item.getName());
			this.category = new SimpleStringProperty(item.getCategory());
			this.amountText = new SimpleStringProperty(formatAmount());
		}

		private String formatAmount() {
			return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString() + " " + unit;
		}

		public int getInventoryId() {
			return inventoryId;
		}

		public double getAmount() {
			return amount;
		}

		public void setAmount(double newAmount) {
			amount = newAmount;
			amountText.set(formatAmount());
		}

		public SimpleStringProperty nameProperty() {
			return name;
		}

		public SimpleStringProperty categoryProperty() {
			return category;
		}

		public SimpleStringProperty amountTextProperty() {
			return amountText;
		}
	}
}
