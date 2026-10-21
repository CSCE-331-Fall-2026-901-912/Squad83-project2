package com.squad83;
import com.squad83.views.ReportsView;
import com.squad83.views.InventoryView;
import com.squad83.views.MenuView;
import com.squad83.views.CashierView;
import com.squad83.views.EmployeeView;
import com.squad83.views.SeasonalItemView;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main JavaFX application for the Squad 83 point-of-sale system. It starts on
 * a role selection screen where the user chooses to continue as a manager or
 * a cashier, and then shows the matching set of views.
 *
 * @author Squad 83
 */
public class App extends Application {

	/**
	 * Starts the application by setting the window title and showing the role
	 * selection screen.
	 *
	 * @param primaryStage the primary window of the application
	 */
	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("Squad 83 - POS System");

		showRoleSelection(primaryStage);
	}

	/**
	 * Shows the role selection screen, which displays the application title and
	 * buttons for choosing the manager or cashier view.
	 *
	 * @param primaryStage the window in which the screen is shown
	 */
	private void showRoleSelection(Stage primaryStage) {

		Label title = new Label("Squad 83 POS System");
		title.setStyle(
				"-fx-font-size: 28px;" +
				"-fx-font-weight: bold;"
		);

		Label subtitle = new Label("Select your role");

		Button managerButton = new Button("Manager");
		managerButton.setPrefSize(150, 50);

		Button cashierButton = new Button("Cashier");
		cashierButton.setPrefSize(150, 50);

		managerButton.setOnAction(e -> showManagerView(primaryStage));
		cashierButton.setOnAction(e -> showCashierView(primaryStage));

		HBox roleButtons = new HBox(
				20,
				managerButton,
				cashierButton
		);

		roleButtons.setAlignment(Pos.CENTER);

		VBox content = new VBox(
				20,
				title,
				subtitle,
				roleButtons
		);

		content.setAlignment(Pos.CENTER);

		StackPane root = new StackPane(content);

		Scene scene = new Scene(root, 1000, 650);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Shows the manager screen, which has tabs for menu items, inventory,
	 * employees, reports, and seasonal items, along with a button that returns
	 * to the role selection screen.
	 *
	 * @param primaryStage the window in which the screen is shown
	 */
	private void showManagerView(Stage primaryStage) {

		BorderPane root = new BorderPane();

		TabPane tabPane = new TabPane();
		tabPane.setSide(Side.TOP);

		Tab menuTab = new Tab(
				"Menu Items",
				MenuView.getView()
		);

		menuTab.setClosable(false);

		Tab inventoryTab = new Tab(
				"Inventory",
				InventoryView.getView()
		);

		inventoryTab.setClosable(false);

		Tab employeeTab = new Tab(
				"Employees",
				EmployeeView.getView()
		);

		employeeTab.setClosable(false);

		Tab reportsTab = new Tab(
				"Reports",
				ReportsView.getView()
		);

		reportsTab.setClosable(false);

		Tab seasonalTab = new Tab(
			"Seasonal Item",
			SeasonalItemView.getView()
		);

		seasonalTab.setClosable(false);

		tabPane.getTabs().addAll(
				menuTab,
				inventoryTab,
				employeeTab,
				reportsTab,
				seasonalTab
		);

		Button homeButton = new Button("Back to Home");
		homeButton.setOnAction(e -> showRoleSelection(primaryStage));

		HBox bottomBar = new HBox(homeButton);
		bottomBar.setAlignment(Pos.CENTER_RIGHT);
		bottomBar.setPadding(new Insets(10, 0, 0, 0));

		root.setCenter(tabPane);
		root.setBottom(bottomBar);

		Scene scene = new Scene(root, 1000, 650);

		primaryStage.setScene(scene);
	}

	/**
	 * Shows the cashier screen, which has a single cashier tab along with a
	 * button that returns to the role selection screen.
	 *
	 * @param primaryStage the window in which the screen is shown
	 */
	private void showCashierView(Stage primaryStage) {

		BorderPane root = new BorderPane();

		TabPane tabPane = new TabPane();
		tabPane.setSide(Side.TOP);

		Tab cashierTab = new Tab(
				"Cashier",
				CashierView.getView()
		);

		cashierTab.setClosable(false);

		tabPane.getTabs().add(cashierTab);

		Button homeButton = new Button("Back to Home");
		homeButton.setOnAction(e -> showRoleSelection(primaryStage));

		HBox bottomBar = new HBox(homeButton);
		bottomBar.setAlignment(Pos.CENTER_RIGHT);
		bottomBar.setPadding(new Insets(10, 0, 0, 0));

		root.setCenter(tabPane);
		root.setBottom(bottomBar);

		Scene scene = new Scene(root, 1000, 650);

		primaryStage.setScene(scene);
	}

	/**
	 * Creates a simple placeholder screen that shows the given name followed by
	 * "View" in a centered label.
	 *
	 * @param name the name displayed in the placeholder label
	 * @return the root node of the placeholder screen
	 */
	private Parent createPlaceholder(String name) {

		Label label = new Label(name + " View");
		label.setStyle("-fx-font-size: 24px;");

		StackPane pane = new StackPane(label);
		pane.setPadding(new Insets(20));

		return pane;
	}

	/**
	 * Launches the JavaFX application.
	 *
	 * @param args the command-line arguments passed to the application
	 */
	public static void main(String[] args) {

		launch(args);
	}
}