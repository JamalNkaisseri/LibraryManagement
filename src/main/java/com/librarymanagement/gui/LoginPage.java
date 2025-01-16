package com.librarymanagement.gui;

import com.lms.User;  // Make sure this import is present
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

    public void showLoginPage(Stage primaryStage, LibraryApp libraryApp) {
        // Labels
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        Label roleLabel = new Label("Role:");

        // Configure text fields
        usernameField.setPromptText("Enter your username");
        passwordField.setPromptText("Enter your password");

        // Dropdown for role selection
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Admin", "User");
        roleComboBox.setValue("User"); // Default role

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
                roleLabel,
                roleComboBox,
                buttonBox,
                statusLabel
        );
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Button actions
        loginButton.setOnAction(event -> handleLogin(primaryStage, libraryApp, roleComboBox.getValue()));
        registerButton.setOnAction(event -> handleRegistration(roleComboBox.getValue()));
        clearButton.setOnAction(event -> clearFields());

        // Scene setup
        Scene scene = new Scene(layout, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Library Management System - Login");
        primaryStage.show();
    }


    private void handleLogin(Stage primaryStage, LibraryApp libraryApp, String role) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        System.out.println("Attempting login with username: " + username); // Debug

        if (validateInput(username, password)) {
            currentUser = new User(username, password);
            System.out.println("Input validated, attempting login..."); // Debug

            if (currentUser.login(username, password)) {
                String fetchedRole = currentUser.getRole();
                System.out.println("Login successful. Fetched role: " + fetchedRole); // Debug

                if ("Admin".equalsIgnoreCase(fetchedRole)) {
                    System.out.println("Role matches Admin, showing admin dashboard"); // Debug
                    libraryApp.showAdminDashboard(primaryStage, currentUser);
                } else {
                    System.out.println("Role does not match Admin, showing main page"); // Debug
                    libraryApp.showMainPage(primaryStage, currentUser);
                }
            } else {
                System.out.println("Login failed in User.login()"); // Debug
                statusLabel.setText("Invalid username or password.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }



    private void handleRegistration(String role) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (validateInput(username, password)) {
            currentUser = new User(username, password);

            if (currentUser.register(role)) {
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
