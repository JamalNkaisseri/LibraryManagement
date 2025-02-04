package com.librarymanagement.gui;

import com.lms.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;

public class UserProfileDialog {
    private final User user;
    private final Stage dialog;
    private Label statusLabel;

    public UserProfileDialog(User user, Stage parentStage) {
        this.user = user;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("User Profile");

        createContent();
    }

    private void createContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f4f6f9;");

        // Profile Information Section
        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(10);
        profileGrid.setVgap(10);
        profileGrid.setPadding(new Insets(10));
        profileGrid.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        // Basic Info
        Label usernameLabel = new Label("Username:");
        Label usernameValue = new Label(user.getUsername());
        usernameValue.setStyle("-fx-font-weight: bold;");

        Label roleLabel = new Label("Role:");
        Label roleValue = new Label(user.getRole());
        roleValue.setStyle("-fx-font-weight: bold;");

        // User Stats
        User.UserStats stats = user.getUserStats();

        Label statsLabel = new Label("Library Statistics");
        statsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label totalBorrowedLabel = new Label("Total Books Borrowed:");
        Label totalBorrowedValue = new Label(String.valueOf(stats.getTotalBorrowed()));

        Label currentlyBorrowedLabel = new Label("Currently Borrowed:");
        Label currentlyBorrowedValue = new Label(String.valueOf(stats.getCurrentlyBorrowed()));

        // Format fines with currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        Label finesLabel = new Label("Total Fines:");
        Label finesValue = new Label(currencyFormat.format(stats.getTotalFines()));
        finesValue.setStyle(stats.getTotalFines() > 0 ? "-fx-text-fill: red;" : "-fx-text-fill: green;");

        // Add components to grid
        profileGrid.addRow(0, usernameLabel, usernameValue);
        profileGrid.addRow(1, roleLabel, roleValue);
        profileGrid.addRow(2, new Separator());
        profileGrid.addRow(3, statsLabel);
        profileGrid.addRow(4, totalBorrowedLabel, totalBorrowedValue);
        profileGrid.addRow(5, currentlyBorrowedLabel, currentlyBorrowedValue);
        profileGrid.addRow(6, finesLabel, finesValue);

        // Password Change Section
        VBox passwordBox = new VBox(10);
        passwordBox.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 10;");

        Label passwordHeader = new Label("Change Password");
        passwordHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current Password");
        currentPasswordField.setMaxWidth(200);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        newPasswordField.setMaxWidth(200);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        confirmPasswordField.setMaxWidth(200);

        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.getStyleClass().add("action-button");
        changePasswordButton.setMaxWidth(200);

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(300);

        changePasswordButton.setOnAction(e -> handlePasswordChange(
                currentPasswordField,
                newPasswordField,
                confirmPasswordField
        ));

        passwordBox.getChildren().addAll(
                passwordHeader,
                currentPasswordField,
                newPasswordField,
                confirmPasswordField,
                changePasswordButton,
                statusLabel
        );
        passwordBox.setAlignment(Pos.CENTER);

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("action-button");
        closeButton.setOnAction(e -> dialog.close());
        closeButton.setMaxWidth(200);

        mainLayout.getChildren().addAll(profileGrid, passwordBox, closeButton);
        mainLayout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(mainLayout, 400, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        dialog.setScene(scene);
        dialog.setResizable(false);
    }

    private void handlePasswordChange(
            PasswordField currentPasswordField,
            PasswordField newPasswordField,
            PasswordField confirmPasswordField
    ) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input fields
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("All fields are required!");
            return;
        }

        // Validate new password length
        if (newPassword.length() < 6) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("New password must be at least 6 characters long!");
            return;
        }

        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("New passwords do not match!");
            return;
        }

        // Attempt to update password
        if (user.updatePassword(currentPassword, newPassword)) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Password updated successfully!");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to update password. Please check your current password.");
        }
    }

    public void show() {
        dialog.showAndWait();
    }
}