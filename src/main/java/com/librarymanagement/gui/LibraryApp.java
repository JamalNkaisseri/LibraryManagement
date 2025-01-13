package com.librarymanagement.gui;

import com.lms.User;  // Make sure this import is present
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LibraryApp extends Application {
    private static Application mainApp;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        mainApp = this;
        this.primaryStage = primaryStage;
        // Display the login page first
        LoginPage loginPage = new LoginPage();
        loginPage.showLoginPage(primaryStage, this);  // Pass LibraryApp instance to LoginPage
    }

    public static Application getMainApp() {
        return mainApp;
    }

    public void showMainPage(Stage primaryStage, User currentUser) {
        // Create UI components for the main page (after login)
        Label label = new Label("Welcome to the Library Management System");
        Button adminButton = new Button("Go to Admin Dashboard");
        Button logoutButton = new Button("Logout");

        // Set up button actions
        adminButton.setOnAction(event -> showAdminDashboard(primaryStage, currentUser));
        logoutButton.setOnAction(event -> {
            LoginPage loginPage = new LoginPage();
            loginPage.showLoginPage(primaryStage, this);  // Pass LibraryApp instance to LoginPage
        });

        // Arrange components in a layout
        VBox layout = new VBox(10); // 10px spacing
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label, adminButton, logoutButton);

        // Create a scene and set it on the stage for the main page
        Scene scene = new Scene(layout, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Library Management System");
        primaryStage.show();
    }

    public void showAdminDashboard(Stage primaryStage, User currentUser) {
        // Pass currentUser to the AdminDashboard constructor
        AdminDashboard dashboard = new AdminDashboard(currentUser);

        // Call the createAdminDashboard method which no longer requires the User object as argument
        Scene adminScene = dashboard.createAdminDashboard(primaryStage);

        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Library Management System - Admin Dashboard");
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
