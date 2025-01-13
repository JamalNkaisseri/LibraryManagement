package com.librarymanagement.gui;

import com.lms.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginPage {
    private TextField usernameField;
    private PasswordField passwordField;
    private Label statusLabel;
    private User currentUser;

    public LoginPage() {
        this.usernameField = new TextField();
        this.passwordField = new PasswordField();
        this.statusLabel = new Label();
    }

    public void showLoginPage(Stage primaryStage) {
        // Labels
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");

        // Configure text fields
        usernameField.setPromptText("Enter your username");
        passwordField.setPromptText("Enter your password");

        // Buttons
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Button clearButton = new Button("Clear");

        // Configure status label
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(300);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, registerButton, clearButton);

        // Main layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                buttonBox,
                statusLabel
        );
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Button actions
        loginButton.setOnAction(event -> handleLogin(primaryStage));
        registerButton.setOnAction(event -> handleRegistration());
        clearButton.setOnAction(event -> clearFields());

        // Scene setup
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Library Management System - Login");
        primaryStage.show();
    }

    private void handleLogin(Stage primaryStage) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (validateInput(username, password)) {
            currentUser = new User(username, password);
            if (currentUser.login(username, password)) {
                statusLabel.setText("Login successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
                // Proceed to main application
                LibraryApp libraryApp = new LibraryApp();
                libraryApp.showMainPage(primaryStage);
            } else {
                statusLabel.setText("Invalid username or password.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void handleRegistration() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (validateInput(username, password)) {
            currentUser = new User(username, password);
            if (currentUser.register()) {
                statusLabel.setText("Registration successful! You can now login.");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Username already exists or registration failed.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return false;
        }
        if (username.length() < 3) {
            statusLabel.setText("Username must be at least 3 characters long.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return false;
        }
        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters long.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return false;
        }
        return true;
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        statusLabel.setText("");
    }
}