package com.librarymanagement.gui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LibraryApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Display the login page first
        LoginPage loginPage = new LoginPage();
        loginPage.showLoginPage(primaryStage);  // Call the showLoginPage method from the LoginPage class
    }

    /**
     * Displays the main application page after a successful login.
     *
     * @param primaryStage The primary stage to display the main page.
     */
    public void showMainPage(Stage primaryStage) {
        // Create UI components for the main page (after login)
        Label label = new Label("Welcome to the Library Management System");
        Button button = new Button("Click Me");

        // Set up button action
        button.setOnAction(event -> System.out.println("Button clicked!"));

        // Arrange components in a layout
        VBox layout = new VBox(10); // 10px spacing
        layout.getChildren().addAll(label, button);

        // Create a scene and set it on the stage for the main page
        Scene scene = new Scene(layout, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Library Management System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);  // Launches the JavaFX application
    }
}
