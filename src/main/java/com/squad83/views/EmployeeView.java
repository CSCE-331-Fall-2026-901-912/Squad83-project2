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

/**
 * JavaFX view for managing the employees of the shop. It displays all
 * employees in a table and provides a form to add, update, activate or
 * deactivate, clear, and refresh employees stored in the database.
 *
 * @author Squad 83
 */
public class EmployeeView {

	private static TableView<Employee> table;
	private static TextField firstNameField;
	private static TextField lastNameField;
	private static ComboBox<String> roleBox;
	private static TextField pinField;
	private static TextField wageField;
	private static DatePicker hireDatePicker;

	/**
	 * Builds the employee management screen, including the title, the employee
	 * table, the input form, and the action buttons. Employees are loaded from
	 * the database before the view is returned.
	 *
	 * @return the root node of the employees view
	 */
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

	/**
	 * Loads all employees from the database, ordered by employee ID, and places
	 * them in the table. If a database error occurs, an error dialog is shown
	 * instead of propagating the exception.
	 */
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

	/**
	 * Validates the form and inserts a new active employee into the database
	 * using the values currently entered. The next employee ID is computed as
	 * part of the insert. On success the table is refreshed and the form is
	 * cleared.
	 */
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

	/**
	 * Updates the employee selected in the table with the values currently in
	 * the form. An error is shown if no employee is selected or the form is
	 * invalid. On success the table is refreshed and the form is cleared.
	 */
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

	/**
	 * Switches the selected employee between active and inactive. The employee
	 * is not deleted; only the active flag is flipped in the database. An error
	 * is shown if no employee is selected. On success the table is refreshed
	 * and the form is cleared.
	 */
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

	/**
	 * Checks that the form fields hold valid input: the first and last names
	 * must not be empty, a role must be chosen, the PIN must be exactly four
	 * digits, the hourly wage must be a non-negative number, and a hire date
	 * must be picked. An error dialog is shown for the first problem found.
	 *
	 * @return {@code true} if all form fields are valid, {@code false} otherwise
	 */
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

	/**
	 * Checks whether the given text is a number that is zero or greater.
	 *
	 * @param text the text to check
	 * @return {@code true} if the text parses as a non-negative number,
	 *         {@code false} otherwise
	 */
	private static boolean isNonNegativeNumber(String text) {

		try {
			return Double.parseDouble(text) >= 0;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Resets the form to its default state by clearing the name, PIN, and wage
	 * fields, removing the role selection, setting the hire date to today, and
	 * clearing the table selection.
	 */
	private static void clearForm() {

		firstNameField.clear();
		lastNameField.clear();
		roleBox.setValue(null);
		pinField.clear();
		wageField.clear();
		hireDatePicker.setValue(LocalDate.now());

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
	 * Model class representing a single row of the employees table. Most
	 * fields are stored as JavaFX properties so the table can bind to them.
	 *
	 * @author Squad 83
	 */
	public static class Employee {

		private final SimpleIntegerProperty id;
		private final SimpleStringProperty firstName;
		private final SimpleStringProperty lastName;
		private final SimpleStringProperty role;
		private final SimpleStringProperty pin;
		private final SimpleDoubleProperty wage;
		private final LocalDate hireDate;
		private final SimpleBooleanProperty active;

		/**
		 * Creates an employee with the given values.
		 *
		 * @param id        the unique employee ID
		 * @param firstName the first name of the employee
		 * @param lastName  the last name of the employee
		 * @param role      the role of the employee, such as manager or cashier
		 * @param pin       the 4-digit PIN of the employee, as text
		 * @param wage      the hourly wage of the employee
		 * @param hireDate  the date the employee was hired
		 * @param active    whether the employee is currently active
		 */
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

		/**
		 * Gets the employee ID.
		 *
		 * @return the employee ID
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
		 * Gets the first name of the employee.
		 *
		 * @return the first name
		 */
		public String getFirstName() {
			return firstName.get();
		}

		/**
		 * Gets the first name property for table binding.
		 *
		 * @return the first name property
		 */
		public SimpleStringProperty firstNameProperty() {
			return firstName;
		}

		/**
		 * Gets the last name of the employee.
		 *
		 * @return the last name
		 */
		public String getLastName() {
			return lastName.get();
		}

		/**
		 * Gets the last name property for table binding.
		 *
		 * @return the last name property
		 */
		public SimpleStringProperty lastNameProperty() {
			return lastName;
		}

		/**
		 * Gets the role of the employee.
		 *
		 * @return the role
		 */
		public String getRole() {
			return role.get();
		}

		/**
		 * Gets the role property for table binding.
		 *
		 * @return the role property
		 */
		public SimpleStringProperty roleProperty() {
			return role;
		}

		/**
		 * Gets the PIN of the employee.
		 *
		 * @return the PIN, as text
		 */
		public String getPin() {
			return pin.get();
		}

		/**
		 * Gets the PIN property for table binding.
		 *
		 * @return the PIN property
		 */
		public SimpleStringProperty pinProperty() {
			return pin;
		}

		/**
		 * Gets the hourly wage of the employee.
		 *
		 * @return the hourly wage
		 */
		public double getWage() {
			return wage.get();
		}

		/**
		 * Gets the wage property for table binding.
		 *
		 * @return the wage property
		 */
		public SimpleDoubleProperty wageProperty() {
			return wage;
		}

		/**
		 * Gets the hire date of the employee.
		 *
		 * @return the hire date
		 */
		public LocalDate getHireDate() {
			return hireDate;
		}

		/**
		 * Gets the employment status of the employee as display text.
		 *
		 * @return {@code "Active"} if the employee is active, {@code "Inactive"}
		 *         otherwise
		 */
		public String getStatus() {
			if (active.get()) {
				return "Active";
			}

			return "Inactive";
		}
	}
}