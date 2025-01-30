package com.librarymanagement.gui;

import com.lms.User;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LibraryApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setMinWidth(1200);  // Set minimum window size
        primaryStage.setMinHeight(800);

        // Display the login page first
        LoginPage loginPage = new LoginPage();
        loginPage.showLoginPage(primaryStage, this);
    }

    // This method will be used to show the main page for regular users
    public void showMainPage(Stage primaryStage, User currentUser) {
        UserDashboard userDashboard = new UserDashboard(currentUser);
        Scene userScene = userDashboard.createUserDashboard(primaryStage, this);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("Library Management System - User Dashboard");
        primaryStage.setMaximized(true);  // Optional: start maximized
        primaryStage.show();
    }

    // This method will be used to show the admin dashboard
    public void showAdminDashboard(Stage primaryStage, User currentUser) {
        AdminDashboard dashboard = new AdminDashboard(currentUser);
        Scene adminScene = dashboard.createAdminDashboard(primaryStage, this);  // Updated to pass LibraryApp instance
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Library Management System - Admin Dashboard");
        primaryStage.setMaximized(true);  // Optional: start maximized
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}