package com.squad83;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Squad 83 - POS System");

        Label label = new Label("Welcome to the POS System");
        Button testDbButton = new Button("Test AWS Connection");
        Label statusLabel = new Label();

        testDbButton.setOnAction(e -> {
            Connection conn = Database.connect();
            if (conn != null) {
                statusLabel.setText("Status: Connected to AWS successfully!");
            } else {
                statusLabel.setText("Status: Connection failed. Check terminal.");
            }
        });

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(label, testDbButton, statusLabel);

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}