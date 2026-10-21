package com.squad83.views;

import com.squad83.Database;

import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cashier view for building and submitting customer orders.
 * <p>
 * Menu items are loaded from the database as buttons grouped by category. The
 * cashier picks the size, sugar level, and ice level, adds drinks to the order,
 * and checks out with a cashier name, payment method, and optional tip.
 * Submitting saves one row to {@code orders}, one row per line to
 * {@code order_items}, and subtracts each drink's recipe from {@code inventory},
 * all in one transaction.
 */
public class CashierView {

	// sales tax rate applied to the subtotal
	private static final BigDecimal TAX_RATE = new BigDecimal("0.0825");

	// extra charge for a large drink
	private static final BigDecimal LARGE_UPCHARGE = new BigDecimal("0.75");

	private static VBox menuSections;
	private static ComboBox<String> sizeBox;
	private static ComboBox<String> sugarBox;
	private static ComboBox<String> iceBox;
	private static TableView<CartItem> cartTable;
	private static ObservableList<CartItem> cart;
	private static ComboBox<EmployeeOption> employeeBox;
	private static ComboBox<String> paymentBox;
	private static TextField tipField;
	private static Label subtotalLabel;
	private static Label taxLabel;
	private static Label totalLabel;

	/**
	 * Builds the cashier view and loads the menu and the list of active employees.
	 *
	 * @return the root node of the view, used by App to fill the Cashier tab
	 */
	public static Parent getView() {

		BorderPane root = new BorderPane();
		root.setPadding(new Insets(15));

		Label title = new Label("Cashier");
		title.setStyle(
				"-fx-font-size: 24px;" +
				"-fx-font-weight: bold;"
		);

		root.setTop(title);

		// left side: drink options and menu item buttons
		sizeBox = new ComboBox<>();
		sizeBox.getItems().addAll("M", "L");
		sizeBox.setValue("M");

		sugarBox = new ComboBox<>();
		sugarBox.getItems().addAll("0%", "25%", "50%", "75%", "100%");
		sugarBox.setValue("100%");

		iceBox = new ComboBox<>();
		iceBox.getItems().addAll("none", "less", "regular");
		iceBox.setValue("regular");

		HBox drinkOptions = new HBox(
				10,
				new Label("Size:"),
				sizeBox,
				new Label("Sugar:"),
				sugarBox,
				new Label("Ice:"),
				iceBox
		);

		drinkOptions.setAlignment(Pos.CENTER_LEFT);

		menuSections = new VBox(15);
		menuSections.setPadding(new Insets(10));

		ScrollPane menuScroll = new ScrollPane(menuSections);
		menuScroll.setFitToWidth(true);

		VBox menuPane = new VBox(
				10,
				drinkOptions,
				menuScroll
		);

		VBox.setVgrow(menuScroll, Priority.ALWAYS);
		HBox.setHgrow(menuPane, Priority.ALWAYS);

		// right side: current order
		Label orderTitle = new Label("Current Order");
		orderTitle.setStyle(
				"-fx-font-size: 18px;" +
				"-fx-font-weight: bold;"
		);

		cart = FXCollections.observableArrayList();

		cartTable = new TableView<>(cart);
		cartTable.setPlaceholder(new Label("No items yet"));

		TableColumn<CartItem, String> itemColumn = new TableColumn<>("Item");
		itemColumn.setCellValueFactory(data -> data.getValue().nameProperty());
		itemColumn.setPrefWidth(140);

		TableColumn<CartItem, String> optionsColumn = new TableColumn<>("Options");
		optionsColumn.setCellValueFactory(data -> data.getValue().optionsProperty());
		optionsColumn.setPrefWidth(100);

		TableColumn<CartItem, Number> quantityColumn = new TableColumn<>("Qty");
		quantityColumn.setCellValueFactory(data -> data.getValue().quantityProperty());
		quantityColumn.setPrefWidth(40);

		TableColumn<CartItem, String> lineTotalColumn = new TableColumn<>("Total");
		lineTotalColumn.setCellValueFactory(data -> data.getValue().lineTotalProperty());
		lineTotalColumn.setPrefWidth(65);

		cartTable.getColumns().addAll(
				itemColumn,
				optionsColumn,
				quantityColumn,
				lineTotalColumn
		);

		Button addOneButton = new Button("+1");
		Button removeOneButton = new Button("-1");
		Button removeItemButton = new Button("Remove");

		addOneButton.setOnAction(e -> changeQuantity(1));
		removeOneButton.setOnAction(e -> changeQuantity(-1));
		removeItemButton.setOnAction(e -> removeItem());

		HBox quantityButtons = new HBox(
				10,
				addOneButton,
				removeOneButton,
				removeItemButton
		);

		employeeBox = new ComboBox<>();
		employeeBox.setPromptText("Select cashier");

		paymentBox = new ComboBox<>();
		paymentBox.getItems().addAll("card", "cash", "mobile");
		paymentBox.setValue("card");

		tipField = new TextField();
		tipField.setPromptText("0.00");
		tipField.textProperty().addListener((obs, oldText, newText) -> updateTotals());

		GridPane checkoutForm = new GridPane();
		checkoutForm.setHgap(10);
		checkoutForm.setVgap(8);

		checkoutForm.add(new Label("Cashier:"), 0, 0);
		checkoutForm.add(employeeBox, 1, 0);

		checkoutForm.add(new Label("Payment:"), 0, 1);
		checkoutForm.add(paymentBox, 1, 1);

		checkoutForm.add(new Label("Tip:"), 0, 2);
		checkoutForm.add(tipField, 1, 2);

		subtotalLabel = new Label("Subtotal: $0.00");
		taxLabel = new Label("Tax: $0.00");

		totalLabel = new Label("Total: $0.00");
		totalLabel.setStyle(
				"-fx-font-size: 20px;" +
				"-fx-font-weight: bold;"
		);

		Button clearButton = new Button("Clear Order");
		Button submitButton = new Button("Submit Order");
		submitButton.setPrefWidth(150);
		submitButton.setStyle("-fx-font-weight: bold;");

		clearButton.setOnAction(e -> clearOrder());
		submitButton.setOnAction(e -> submitOrder());

		HBox orderButtons = new HBox(
				10,
				clearButton,
				submitButton
		);

		orderButtons.setAlignment(Pos.CENTER_RIGHT);

		VBox orderPane = new VBox(
				10,
				orderTitle,
				cartTable,
				quantityButtons,
				checkoutForm,
				subtotalLabel,
				taxLabel,
				totalLabel,
				orderButtons
		);

		orderPane.setPrefWidth(380);
		orderPane.setMinWidth(380);

		VBox.setVgrow(cartTable, Priority.ALWAYS);

		HBox content = new HBox(
				15,
				menuPane,
				orderPane
		);

		content.setPadding(new Insets(15, 0, 0, 0));

		root.setCenter(content);

		loadMenu();
		loadEmployees();

		return root;
	}

	/**
	 * Loads all active menu items and shows each as a button, grouped under a
	 * heading for its category. Clicking a button adds that drink to the order.
	 */
	private static void loadMenu() {

		menuSections.getChildren().clear();

		Map<String, FlowPane> categories = new LinkedHashMap<>();

		String sql =
				"SELECT menu_item_id, name, category, base_price " +
				"FROM menu_items " +
				"WHERE is_active = true " +
				"ORDER BY category, name";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()
		) {
			while (rs.next()) {
				String category = rs.getString("category");

				// first item in a category creates that category's section
				if (!categories.containsKey(category)) {
					Label categoryLabel = new Label(category);
					categoryLabel.setStyle(
							"-fx-font-size: 16px;" +
							"-fx-font-weight: bold;"
					);

					FlowPane buttons = new FlowPane(10, 10);

					categories.put(category, buttons);
					menuSections.getChildren().addAll(categoryLabel, buttons);
				}

				MenuOption option = new MenuOption(
						rs.getInt("menu_item_id"),
						rs.getString("name"),
						rs.getBigDecimal("base_price")
				);

				Button itemButton = new Button(option.getName() + "\n$" + option.getBasePrice());
				itemButton.setPrefSize(150, 60);
				itemButton.setWrapText(true);
				itemButton.setOnAction(e -> addToCart(option));

				categories.get(category).getChildren().add(itemButton);
			}

		} catch (SQLException e) {
			showError("Could not load menu.", e.getMessage());
		}
	}

	/**
	 * Loads all active employees into the cashier drop-down.
	 */
	private static void loadEmployees() {

		ObservableList<EmployeeOption> employees = FXCollections.observableArrayList();

		String sql =
				"SELECT employee_id, first_name, last_name " +
				"FROM employees " +
				"WHERE is_active = true " +
				"ORDER BY first_name, last_name";

		try (
				Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()
		) {
			while (rs.next()) {
				employees.add(
						new EmployeeOption(
								rs.getInt("employee_id"),
								rs.getString("first_name") + " " + rs.getString("last_name")
						)
				);
			}

			employeeBox.setItems(employees);

		} catch (SQLException e) {
			showError("Could not load employees.", e.getMessage());
		}
	}

	/**
	 * Adds a drink to the order using the currently selected size, sugar, and ice.
	 * If the same drink with the same options is already in the order, its quantity
	 * goes up by one instead. Large drinks cost the base price plus the large upcharge.
	 *
	 * @param option the menu item that was clicked
	 */
	private static void addToCart(MenuOption option) {

		String size = sizeBox.getValue();
		String sugar = sugarBox.getValue();
		String ice = iceBox.getValue();

		// same drink with the same options just increases the quantity
		for (CartItem item : cart) {
			if (item.matches(option.getId(), size, sugar, ice)) {
				item.setQuantity(item.getQuantity() + 1);
				cartTable.getSelectionModel().select(item);
				updateTotals();
				return;
			}
		}

		BigDecimal unitPrice = option.getBasePrice();

		if (size.equals("L")) {
			unitPrice = unitPrice.add(LARGE_UPCHARGE);
		}

		CartItem newItem = new CartItem(option.getId(), option.getName(), size, sugar, ice, unitPrice);

		cart.add(newItem);
		cartTable.getSelectionModel().select(newItem);

		updateTotals();
	}

	/**
	 * Changes the quantity of the selected order line. A line whose quantity drops
	 * to zero is removed.
	 *
	 * @param change the amount to add to the quantity, such as 1 or -1
	 */
	private static void changeQuantity(int change) {

		CartItem selected = cartTable.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No item selected.", "Select an item in the order first.");
			return;
		}

		int newQuantity = selected.getQuantity() + change;

		if (newQuantity <= 0) {
			cart.remove(selected);
		} else {
			selected.setQuantity(newQuantity);
		}

		updateTotals();
	}

	/**
	 * Removes the selected line from the order, or shows an error if none is selected.
	 */
	private static void removeItem() {

		CartItem selected = cartTable.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No item selected.", "Select an item in the order first.");
			return;
		}

		cart.remove(selected);

		updateTotals();
	}

	/**
	 * Empties the order, clears the tip, and resets the payment method to card.
	 */
	private static void clearOrder() {

		cart.clear();
		tipField.clear();
		paymentBox.setValue("card");

		updateTotals();
	}

	/**
	 * Adds up the line totals of every item in the order.
	 *
	 * @return the order subtotal, rounded to cents
	 */
	private static BigDecimal getSubtotal() {

		BigDecimal subtotal = BigDecimal.ZERO;

		for (CartItem item : cart) {
			subtotal = subtotal.add(item.getLineTotal());
		}

		return subtotal.setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Calculates sales tax on a subtotal.
	 *
	 * @param subtotal the order subtotal
	 * @return the tax, rounded to cents
	 */
	private static BigDecimal getTax(BigDecimal subtotal) {

		return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
	}

	// returns null when the tip text is not a valid amount
	/**
	 * Reads the tip from the tip field. An empty field counts as no tip.
	 *
	 * @return the tip rounded to cents, or null if the text is not a valid
	 *         non-negative number
	 */
	private static BigDecimal getTip() {

		String text = tipField.getText().trim();

		if (text.isEmpty()) {
			return BigDecimal.ZERO.setScale(2);
		}

		try {
			BigDecimal tip = new BigDecimal(text);

			if (tip.compareTo(BigDecimal.ZERO) < 0) {
				return null;
			}

			return tip.setScale(2, RoundingMode.HALF_UP);

		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Recalculates the subtotal, tax, and total and updates their labels. An invalid
	 * tip is treated as zero here; it is rejected when the order is submitted.
	 */
	private static void updateTotals() {

		BigDecimal subtotal = getSubtotal();
		BigDecimal tax = getTax(subtotal);
		BigDecimal tip = getTip();

		if (tip == null) {
			tip = BigDecimal.ZERO;
		}

		subtotalLabel.setText("Subtotal: $" + subtotal);
		taxLabel.setText("Tax: $" + tax);
		totalLabel.setText("Total: $" + subtotal.add(tax).add(tip).setScale(2, RoundingMode.HALF_UP));
	}

	/**
	 * Validates the order, asks for confirmation, and saves it in one transaction:
	 * one row in {@code orders}, one row per line in {@code order_items}, and the
	 * recipe amounts subtracted from {@code inventory}, never going below zero. If
	 * any step fails, the whole transaction is rolled back and nothing is saved.
	 */
	private static void submitOrder() {

		if (cart.isEmpty()) {
			showError("Empty order.", "Add at least one item before submitting.");
			return;
		}

		EmployeeOption employee = employeeBox.getValue();

		if (employee == null) {
			showError("No cashier selected.", "Select the cashier taking this order.");
			return;
		}

		BigDecimal tip = getTip();

		if (tip == null) {
			showError("Invalid tip.", "Tip must be a number that is 0 or greater, such as 1.50.");
			return;
		}

		BigDecimal subtotal = getSubtotal();
		BigDecimal tax = getTax(subtotal);
		BigDecimal total = subtotal.add(tax).add(tip).setScale(2, RoundingMode.HALF_UP);

		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Submit Order");
		confirmation.setHeaderText("Submit order for $" + total + "?");
		confirmation.setContentText("Paid by " + paymentBox.getValue() + ", rung up by " + employee + ".");

		ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);

		if (result != ButtonType.OK) {
			return;
		}

		String orderSql =
				"INSERT INTO orders " +
				"(order_time, employee_id, subtotal, tax, tip, total, payment_method, status) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, 'completed') " +
				"RETURNING order_id";

		String itemSql =
				"INSERT INTO order_items " +
				"(order_id, menu_item_id, quantity, size, sugar_level, ice_level, unit_price) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)";

		// subtract each drink's recipe from inventory, never going below 0
		String inventorySql =
				"UPDATE inventory i " +
				"SET quantity_on_hand = GREATEST(i.quantity_on_hand - mii.quantity_used * ?, 0) " +
				"FROM menu_item_ingredients mii " +
				"WHERE mii.inventory_id = i.inventory_id " +
				"AND mii.menu_item_id = ?";

		int orderId;

		try (Connection conn = Database.getConnection()) {

			conn.setAutoCommit(false);

			try (
					PreparedStatement orderStmt = conn.prepareStatement(orderSql);
					PreparedStatement itemStmt = conn.prepareStatement(itemSql);
					PreparedStatement inventoryStmt = conn.prepareStatement(inventorySql)
			) {
				orderStmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
				orderStmt.setInt(2, employee.getId());
				orderStmt.setBigDecimal(3, subtotal);
				orderStmt.setBigDecimal(4, tax);
				orderStmt.setBigDecimal(5, tip);
				orderStmt.setBigDecimal(6, total);
				orderStmt.setString(7, paymentBox.getValue());

				try (ResultSet rs = orderStmt.executeQuery()) {
					rs.next();
					orderId = rs.getInt("order_id");
				}

				for (CartItem item : cart) {
					itemStmt.setInt(1, orderId);
					itemStmt.setInt(2, item.getMenuItemId());
					itemStmt.setInt(3, item.getQuantity());
					itemStmt.setString(4, item.getSize());
					itemStmt.setString(5, item.getSugar());
					itemStmt.setString(6, item.getIce());
					itemStmt.setBigDecimal(7, item.getUnitPrice());
					itemStmt.addBatch();

					inventoryStmt.setInt(1, item.getQuantity());
					inventoryStmt.setInt(2, item.getMenuItemId());
					inventoryStmt.addBatch();
				}

				itemStmt.executeBatch();
				inventoryStmt.executeBatch();

				conn.commit();

			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}

		} catch (SQLException e) {
			showError("Could not submit order.", e.getMessage());
			return;
		}

		Alert success = new Alert(Alert.AlertType.INFORMATION);
		success.setTitle("Order Submitted");
		success.setHeaderText(null);
		success.setContentText("Order #" + orderId + " submitted. Total: $" + total);
		success.showAndWait();

		clearOrder();
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
	 * A menu item shown as a button on the cashier screen.
	 */
	public static class MenuOption {

		private final int id;
		private final String name;
		private final BigDecimal basePrice;

		/**
		 * Creates a menu option.
		 *
		 * @param id the item's menu_item_id
		 * @param name the item's name
		 * @param basePrice the item's medium-size price
		 */
		public MenuOption(int id, String name, BigDecimal basePrice) {

			this.id = id;
			this.name = name;
			this.basePrice = basePrice;
		}

		/**
		 * Returns the item's menu_item_id.
		 *
		 * @return the item's menu_item_id
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
		 * Returns the item's medium-size price.
		 *
		 * @return the item's medium-size price
		 */
		public BigDecimal getBasePrice() {
			return basePrice;
		}
	}

	/**
	 * An employee shown in the cashier drop-down.
	 */
	public static class EmployeeOption {

		private final int id;
		private final String name;

		/**
		 * Creates an employee option.
		 *
		 * @param id the employee's employee_id
		 * @param name the employee's full name
		 */
		public EmployeeOption(int id, String name) {

			this.id = id;
			this.name = name;
		}

		/**
		 * Returns the employee's employee_id.
		 *
		 * @return the employee's employee_id
		 */
		public int getId() {
			return id;
		}

		// shown in the cashier drop-down
		/**
		 * Returns the employee's full name, shown in the drop-down.
		 *
		 * @return the employee's full name, shown in the drop-down
		 */
		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * One line in the current order: a drink with its size, sugar level, ice level,
	 * price, and quantity.
	 */
	public static class CartItem {

		private final int menuItemId;
		private final String size;
		private final String sugar;
		private final String ice;
		private final BigDecimal unitPrice;
		private final SimpleStringProperty name;
		private final SimpleStringProperty options;
		private final SimpleIntegerProperty quantity;
		private final SimpleStringProperty lineTotal;

		/**
		 * Creates an order line with a quantity of one.
		 *
		 * @param menuItemId the drink's menu_item_id
		 * @param name the drink's name
		 * @param size the size, M or L
		 * @param sugar the sugar level, from 0% to 100%
		 * @param ice the ice level: none, less, or regular
		 * @param unitPrice the price of one drink, including any large upcharge
		 */
		public CartItem(int menuItemId, String name, String size, String sugar, String ice, BigDecimal unitPrice) {

			this.menuItemId = menuItemId;
			this.size = size;
			this.sugar = sugar;
			this.ice = ice;
			this.unitPrice = unitPrice;
			this.name = new SimpleStringProperty(name);
			this.options = new SimpleStringProperty(size + ", " + sugar + ", " + ice);
			this.quantity = new SimpleIntegerProperty(1);
			this.lineTotal = new SimpleStringProperty("$" + unitPrice);
		}

		/**
		 * Checks whether this line is the same drink with the same options.
		 *
		 * @param otherId the menu_item_id to compare
		 * @param otherSize the size to compare
		 * @param otherSugar the sugar level to compare
		 * @param otherIce the ice level to compare
		 * @return true if every value matches this line
		 */
		public boolean matches(int otherId, String otherSize, String otherSugar, String otherIce) {
			return menuItemId == otherId && size.equals(otherSize) && sugar.equals(otherSugar) && ice.equals(otherIce);
		}

		/**
		 * Returns the drink's menu_item_id.
		 *
		 * @return the drink's menu_item_id
		 */
		public int getMenuItemId() {
			return menuItemId;
		}

		/**
		 * Returns the drink's size.
		 *
		 * @return the drink's size
		 */
		public String getSize() {
			return size;
		}

		/**
		 * Returns the drink's sugar level.
		 *
		 * @return the drink's sugar level
		 */
		public String getSugar() {
			return sugar;
		}

		/**
		 * Returns the drink's ice level.
		 *
		 * @return the drink's ice level
		 */
		public String getIce() {
			return ice;
		}

		/**
		 * Returns the price of one drink.
		 *
		 * @return the price of one drink
		 */
		public BigDecimal getUnitPrice() {
			return unitPrice;
		}

		/**
		 * Returns the drink's name.
		 *
		 * @return the drink's name
		 */
		public String getName() {
			return name.get();
		}

		/**
		 * Returns the drink name, bound to the Item column.
		 *
		 * @return the drink name, bound to the Item column
		 */
		public SimpleStringProperty nameProperty() {
			return name;
		}

		/**
		 * Returns the size, sugar, and ice text, bound to the Options column.
		 *
		 * @return the size, sugar, and ice text, bound to the Options column
		 */
		public SimpleStringProperty optionsProperty() {
			return options;
		}

		/**
		 * Returns the number of this drink in the order.
		 *
		 * @return the number of this drink in the order
		 */
		public int getQuantity() {
			return quantity.get();
		}

		/**
		 * Changes the quantity and updates the displayed line total.
		 *
		 * @param newQuantity the new quantity
		 */
		public void setQuantity(int newQuantity) {
			quantity.set(newQuantity);
			lineTotal.set("$" + getLineTotal().setScale(2, RoundingMode.HALF_UP));
		}

		/**
		 * Returns the quantity, bound to the Qty column.
		 *
		 * @return the quantity, bound to the Qty column
		 */
		public SimpleIntegerProperty quantityProperty() {
			return quantity;
		}

		/**
		 * Returns the unit price times the quantity.
		 *
		 * @return the unit price times the quantity
		 */
		public BigDecimal getLineTotal() {
			return unitPrice.multiply(BigDecimal.valueOf(getQuantity()));
		}

		/**
		 * Returns the formatted line total, bound to the Total column.
		 *
		 * @return the formatted line total, bound to the Total column
		 */
		public SimpleStringProperty lineTotalProperty() {
			return lineTotal;
		}
	}
}