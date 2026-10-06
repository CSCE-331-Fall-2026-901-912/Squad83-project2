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

	private static void removeItem() {

		CartItem selected = cartTable.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No item selected.", "Select an item in the order first.");
			return;
		}

		cart.remove(selected);

		updateTotals();
	}

	private static void clearOrder() {

		cart.clear();
		tipField.clear();
		paymentBox.setValue("card");

		updateTotals();
	}

	private static BigDecimal getSubtotal() {

		BigDecimal subtotal = BigDecimal.ZERO;

		for (CartItem item : cart) {
			subtotal = subtotal.add(item.getLineTotal());
		}

		return subtotal.setScale(2, RoundingMode.HALF_UP);
	}

	private static BigDecimal getTax(BigDecimal subtotal) {

		return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
	}

	// returns null when the tip text is not a valid amount
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

	private static void showError(String title, String message) {

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static class MenuOption {

		private final int id;
		private final String name;
		private final BigDecimal basePrice;

		public MenuOption(int id, String name, BigDecimal basePrice) {

			this.id = id;
			this.name = name;
			this.basePrice = basePrice;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public BigDecimal getBasePrice() {
			return basePrice;
		}
	}

	public static class EmployeeOption {

		private final int id;
		private final String name;

		public EmployeeOption(int id, String name) {

			this.id = id;
			this.name = name;
		}

		public int getId() {
			return id;
		}

		// shown in the cashier drop-down
		@Override
		public String toString() {
			return name;
		}
	}

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

		public boolean matches(int otherId, String otherSize, String otherSugar, String otherIce) {
			return menuItemId == otherId && size.equals(otherSize) && sugar.equals(otherSugar) && ice.equals(otherIce);
		}

		public int getMenuItemId() {
			return menuItemId;
		}

		public String getSize() {
			return size;
		}

		public String getSugar() {
			return sugar;
		}

		public String getIce() {
			return ice;
		}

		public BigDecimal getUnitPrice() {
			return unitPrice;
		}

		public String getName() {
			return name.get();
		}

		public SimpleStringProperty nameProperty() {
			return name;
		}

		public SimpleStringProperty optionsProperty() {
			return options;
		}

		public int getQuantity() {
			return quantity.get();
		}

		public void setQuantity(int newQuantity) {
			quantity.set(newQuantity);
			lineTotal.set("$" + getLineTotal().setScale(2, RoundingMode.HALF_UP));
		}

		public SimpleIntegerProperty quantityProperty() {
			return quantity;
		}

		public BigDecimal getLineTotal() {
			return unitPrice.multiply(BigDecimal.valueOf(getQuantity()));
		}

		public SimpleStringProperty lineTotalProperty() {
			return lineTotal;
		}
	}
}