package com.librarymanagement.gui;

import com.lms.DatabaseConnection;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginPage {

    private DatabaseConnection databaseConnection = new DatabaseConnection();  // Reference to the DatabaseConnection class

    public void showLoginPage(Stage primaryStage) {
        // UI Components for the login page
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button loginButton = new Button("Login");
        Label statusLabel = new Label();

        // Action on login button
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Authenticate user using the DatabaseConnection class
            if (authenticateUser(username, password)) {
                statusLabel.setText("Login successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
                // Proceed to the main application window (e.g., the library management dashboard)
                LibraryApp libraryApp = new LibraryApp();
                libraryApp.showMainPage(primaryStage);  // Call the method in LibraryApp to show main page
            } else {
                statusLabel.setText("Invalid username or password.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        // Layout for the login form
        VBox layout = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, statusLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Scene setup for login page
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    /**
     * Authenticates the user by checking the provided username and password with the database.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return true if the username and password are valid, false otherwise.
     */
    private boolean authenticateUser(String username, String password) {
        return databaseConnection.authenticateUser(username, password); // Calls the authenticateUser method from DatabaseConnection
    }
}
