package com.squad83;
import com.squad83.views.ReportsView;
import com.squad83.views.InventoryView;
import com.squad83.views.MenuView;
import com.squad83.views.CashierView;
import com.squad83.views.EmployeeView;

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

public class App extends Application {

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("Squad 83 - POS System");

		showRoleSelection(primaryStage);
	}

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

		tabPane.getTabs().addAll(
				menuTab,
				inventoryTab,
				employeeTab,
				reportsTab
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

	private Parent createPlaceholder(String name) {

		Label label = new Label(name + " View");
		label.setStyle("-fx-font-size: 24px;");

		StackPane pane = new StackPane(label);
		pane.setPadding(new Insets(20));

		return pane;
	}

	public static void main(String[] args) {

		launch(args);
	}
}