package com.squad83.views;

import com.squad83.Database;

import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.DatePicker;
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

public class EmployeeView {

	private static TableView<Employee> table;
	private static TextField firstNameField;
	private static TextField lastNameField;
	private static ComboBox<String> roleBox;
	private static TextField pinField;
	private static TextField wageField;
	private static DatePicker hireDatePicker;

	public static Parent getView() {

		BorderPane root = new BorderPane();
		root.setPadding(new Insets(15));

		Label title = new Label("Employees");
		title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

		root.setTop(title);

		table = new TableView<>();

		TableColumn<Employee, Number> idColumn = new TableColumn<>("ID");
		idColumn.setCellValueFactory(data -> data.getValue().idProperty());

		TableColumn<Employee, String> firstNameColumn = new TableColumn<>("First Name");
		firstNameColumn.setCellValueFactory(data -> data.getValue().firstNameProperty());

		TableColumn<Employee, String> lastNameColumn = new TableColumn<>("Last Name");
		lastNameColumn.setCellValueFactory(data -> data.getValue().lastNameProperty());

		TableColumn<Employee, String> roleColumn = new TableColumn<>("Role");
		roleColumn.setCellValueFactory(data -> data.getValue().roleProperty());

		TableColumn<Employee, String> pinColumn = new TableColumn<>("PIN");
		pinColumn.setCellValueFactory(data -> data.getValue().pinProperty());

		TableColumn<Employee, Number> wageColumn = new TableColumn<>("Hourly Wage");
		wageColumn.setCellValueFactory(data -> data.getValue().wageProperty());

		TableColumn<Employee, String> hireDateColumn = new TableColumn<>("Hire Date");
		hireDateColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getHireDate())));

		TableColumn<Employee, String> statusColumn = new TableColumn<>("Status");
		statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

		table.getColumns().addAll(idColumn, firstNameColumn, lastNameColumn, roleColumn, pinColumn, wageColumn, hireDateColumn, statusColumn);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
			if (newItem != null) {
				firstNameField.setText(newItem.getFirstName());
				lastNameField.setText(newItem.getLastName());
				roleBox.setValue(newItem.getRole());
				pinField.setText(newItem.getPin());
				wageField.setText(String.valueOf(newItem.getWage()));
				hireDatePicker.setValue(newItem.getHireDate());
			}
		});

		firstNameField = new TextField();
		firstNameField.setPromptText("First name");

		lastNameField = new TextField();
		lastNameField.setPromptText("Last name");

		roleBox = new ComboBox<>();
		roleBox.getItems().addAll("manager", "cashier");

		pinField = new TextField();
		pinField.setPromptText("4-digit PIN");

		wageField = new TextField();
		wageField.setPromptText("Hourly wage");

		hireDatePicker = new DatePicker(LocalDate.now());

		Button addButton = new Button("Add Employee");
		Button updateButton = new Button("Update Employee");
		Button toggleButton = new Button("Activate / Deactivate");
		Button clearButton = new Button("Clear");
		Button refreshButton = new Button("Refresh");

		addButton.setOnAction(e -> addEmployee());
		updateButton.setOnAction(e -> updateEmployee());
		toggleButton.setOnAction(e -> toggleActive());
		clearButton.setOnAction(e -> clearForm());
		refreshButton.setOnAction(e -> loadEmployees());

		HBox buttons = new HBox(10, addButton, updateButton, toggleButton, clearButton, refreshButton);

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);
		form.setPadding(new Insets(15, 0, 0, 0));

		form.add(new Label("First Name:"), 0, 0);
		form.add(firstNameField, 1, 0);

		form.add(new Label("Last Name:"), 2, 0);
		form.add(lastNameField, 3, 0);

		form.add(new Label("Role:"), 4, 0);
		form.add(roleBox, 5, 0);

		form.add(new Label("PIN:"), 0, 1);
		form.add(pinField, 1, 1);

		form.add(new Label("Hourly Wage:"), 2, 1);
		form.add(wageField, 3, 1);

		form.add(new Label("Hire Date:"), 4, 1);
		form.add(hireDatePicker, 5, 1);

		form.add(buttons, 0, 2, 6, 1);

		VBox content = new VBox(15, table, form);

		VBox.setVgrow(table, Priority.ALWAYS);

		root.setCenter(content);

		loadEmployees();

		return root;
	}

	private static void loadEmployees() {

		ObservableList<Employee> employees = FXCollections.observableArrayList();

		String sql =
				"SELECT employee_id, first_name, last_name, role, pin, hourly_wage, hire_date, is_active " +
				"FROM employees " +
				"ORDER BY employee_id";

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				employees.add(new Employee(rs.getInt("employee_id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("role"), rs.getString("pin"), rs.getDouble("hourly_wage"), rs.getDate("hire_date").toLocalDate(), rs.getBoolean("is_active")));
			}

			table.setItems(employees);

		} catch (SQLException e) {
			showError("Could not load employees.", e.getMessage());
		}
	}

	private static void addEmployee() {

		if (!validateForm()) {
			return;
		}

		// employee_id has no auto-increment, so the next id is computed in the insert
		String sql =
				"INSERT INTO employees " +
				"(employee_id, first_name, last_name, role, pin, hourly_wage, hire_date, is_active) " +
				"VALUES ((SELECT COALESCE(MAX(employee_id), 0) + 1 FROM employees), ?, ?, ?, ?, ?, ?, TRUE)";

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, firstNameField.getText().trim());
			stmt.setString(2, lastNameField.getText().trim());
			stmt.setString(3, roleBox.getValue());
			stmt.setObject(4, pinField.getText().trim(), Types.OTHER);
			stmt.setDouble(5, Double.parseDouble(wageField.getText()));
			stmt.setDate(6, Date.valueOf(hireDatePicker.getValue()));

			stmt.executeUpdate();

			loadEmployees();
			clearForm();

		} catch (SQLException e) {
			showError("Could not add employee.", e.getMessage());
		}
	}

	private static void updateEmployee() {

		Employee selected = table.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No employee selected.", "Select an employee from the table first.");
			return;
		}

		if (!validateForm()) {
			return;
		}

		String sql =
				"UPDATE employees " +
				"SET first_name = ?, " +
				"last_name = ?, " +
				"role = ?, " +
				"pin = ?, " +
				"hourly_wage = ?, " +
				"hire_date = ? " +
				"WHERE employee_id = ?";

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, firstNameField.getText().trim());
			stmt.setString(2, lastNameField.getText().trim());
			stmt.setString(3, roleBox.getValue());
			stmt.setObject(4, pinField.getText().trim(), Types.OTHER);
			stmt.setDouble(5, Double.parseDouble(wageField.getText()));
			stmt.setDate(6, Date.valueOf(hireDatePicker.getValue()));
			stmt.setInt(7, selected.getId());

			stmt.executeUpdate();

			loadEmployees();
			clearForm();

		} catch (SQLException e) {
			showError("Could not update employee.", e.getMessage());
		}
	}

	private static void toggleActive() {

		Employee selected = table.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("No employee selected.", "Select an employee from the table first.");
			return;
		}

		// employees are deactivated instead of deleted so past orders keep their cashier
		String sql = "UPDATE employees SET is_active = NOT is_active WHERE employee_id = ?";

		try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, selected.getId());

			stmt.executeUpdate();

			loadEmployees();
			clearForm();

		} catch (SQLException e) {
			showError("Could not change employee status.", e.getMessage());
		}
	}

	private static boolean validateForm() {

		if (firstNameField.getText().trim().isEmpty()) {
			showError("Invalid first name.", "First name cannot be empty.");
			return false;
		}

		if (lastNameField.getText().trim().isEmpty()) {
			showError("Invalid last name.", "Last name cannot be empty.");
			return false;
		}

		if (roleBox.getValue() == null) {
			showError("Invalid role.", "Please select a role.");
			return false;
		}

		if (!pinField.getText().trim().matches("\\d{4}")) {
			showError("Invalid PIN.", "PIN must be exactly 4 digits.");
			return false;
		}

		if (!isNonNegativeNumber(wageField.getText())) {
			showError("Invalid wage.", "Hourly wage must be a number that is 0 or greater, such as 12.50.");
			return false;
		}

		if (hireDatePicker.getValue() == null) {
			showError("Invalid hire date.", "Please pick a hire date.");
			return false;
		}

		return true;
	}

	private static boolean isNonNegativeNumber(String text) {

		try {
			return Double.parseDouble(text) >= 0;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static void clearForm() {

		firstNameField.clear();
		lastNameField.clear();
		roleBox.setValue(null);
		pinField.clear();
		wageField.clear();
		hireDatePicker.setValue(LocalDate.now());

		table.getSelectionModel().clearSelection();
	}

	private static void showError(String title, String message) {

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static class Employee {

		private final SimpleIntegerProperty id;
		private final SimpleStringProperty firstName;
		private final SimpleStringProperty lastName;
		private final SimpleStringProperty role;
		private final SimpleStringProperty pin;
		private final SimpleDoubleProperty wage;
		private final LocalDate hireDate;
		private final SimpleBooleanProperty active;

		public Employee(int id, String firstName, String lastName, String role, String pin, double wage, LocalDate hireDate, boolean active) {

			this.id = new SimpleIntegerProperty(id);
			this.firstName = new SimpleStringProperty(firstName);
			this.lastName = new SimpleStringProperty(lastName);
			this.role = new SimpleStringProperty(role);
			this.pin = new SimpleStringProperty(pin);
			this.wage = new SimpleDoubleProperty(wage);
			this.hireDate = hireDate;
			this.active = new SimpleBooleanProperty(active);
		}

		public int getId() {
			return id.get();
		}

		public SimpleIntegerProperty idProperty() {
			return id;
		}

		public String getFirstName() {
			return firstName.get();
		}

		public SimpleStringProperty firstNameProperty() {
			return firstName;
		}

		public String getLastName() {
			return lastName.get();
		}

		public SimpleStringProperty lastNameProperty() {
			return lastName;
		}

		public String getRole() {
			return role.get();
		}

		public SimpleStringProperty roleProperty() {
			return role;
		}

		public String getPin() {
			return pin.get();
		}

		public SimpleStringProperty pinProperty() {
			return pin;
		}

		public double getWage() {
			return wage.get();
		}

		public SimpleDoubleProperty wageProperty() {
			return wage;
		}

		public LocalDate getHireDate() {
			return hireDate;
		}

		public String getStatus() {
			if (active.get()) {
				return "Active";
			}

			return "Inactive";
		}
	}
}
