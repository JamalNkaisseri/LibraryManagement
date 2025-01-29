package com.librarymanagement.gui;

import com.lms.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.getStyleClass().add("login-background");

        Label titleLabel = new Label("Library Management System");
        titleLabel.getStyleClass().add("title-label");

        GridPane loginForm = createLoginForm(primaryStage, libraryApp);

        statusLabel.getStyleClass().add("status-label");

        mainContainer.getChildren().addAll(titleLabel, loginForm, statusLabel);

        Scene scene = new Scene(mainContainer, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/login-styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Library Management System");
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private GridPane createLoginForm(Stage primaryStage, LibraryApp libraryApp) {
        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setHgap(10);
        form.setVgap(15);
        form.getStyleClass().add("login-form");

        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("input-field");

        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("input-field");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("action-button");
        loginButton.setOnAction(event -> handleLogin(primaryStage, libraryApp));

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("action-button");
        registerButton.setOnAction(event -> handleRegistration());

        Button clearButton = new Button("Clear");
        clearButton.getStyleClass().add("action-button");
        clearButton.setOnAction(event -> clearFields());

        buttonBox.getChildren().addAll(loginButton, registerButton, clearButton);

        form.add(usernameLabel, 0, 0);
        form.add(usernameField, 1, 0);
        form.add(passwordLabel, 0, 1);
        form.add(passwordField, 1, 1);
        form.add(buttonBox, 1, 2);

        return form;
    }



    private void handleLogin(Stage primaryStage, LibraryApp libraryApp) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (validateInput(username, password)) {
            currentUser = new User(username, password);

            if (currentUser.login(username, password)) {
                // Check the role to determine if the user is an admin
                if ("admin".equalsIgnoreCase(currentUser.getRole())) {
                    libraryApp.showAdminDashboard(primaryStage, currentUser);
                } else {
                    libraryApp.showMainPage(primaryStage, currentUser);
                }
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

            if (currentUser.register("User")) {
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