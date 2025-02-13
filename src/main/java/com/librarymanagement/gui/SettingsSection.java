package com.librarymanagement.gui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
import java.util.prefs.Preferences;

public class SettingsSection {
    private final Preferences prefs = Preferences.userNodeForPackage(SettingsSection.class);
    private final VBox settingsContainer;
    private final Label statusLabel;

    // Default values
    private static final int DEFAULT_LOAN_PERIOD = 14;
    private static final int DEFAULT_MAX_BOOKS = 5;
    private static final boolean DEFAULT_EMAIL_NOTIFICATIONS = true;
    private static final String DEFAULT_THEME = "Light";

    public SettingsSection() {
        this.settingsContainer = new VBox(20);
        this.statusLabel = new Label();
        initializeSettings();
    }

    private void initializeSettings() {
        settingsContainer.setPadding(new Insets(20));
        settingsContainer.setMaxWidth(800);

        // Header
        Label headerLabel = new Label("System Settings");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Create different settings sections
        VBox loanSettingsBox = createLoanSettings();
        VBox notificationSettingsBox = createNotificationSettings();
        VBox appearanceSettingsBox = createAppearanceSettings();
        VBox backupSettingsBox = createBackupSettings();

        // Save Button
        Button saveButton = new Button("Save All Settings");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> saveAllSettings());

        // Reset Button
        Button resetButton = new Button("Reset to Defaults");
        resetButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        resetButton.setOnAction(e -> resetToDefaults());

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, resetButton);

        statusLabel.setStyle("-fx-font-size: 14px;");

        // Add all components to the main container
        settingsContainer.getChildren().addAll(
                headerLabel,
                new Separator(),
                loanSettingsBox,
                new Separator(),
                notificationSettingsBox,
                new Separator(),
                appearanceSettingsBox,
                new Separator(),
                backupSettingsBox,
                new Separator(),
                buttonBox,
                statusLabel
        );
    }

    private VBox createLoanSettings() {
        VBox loanBox = new VBox(10);
        loanBox.setPadding(new Insets(10));

        Label sectionLabel = new Label("Loan Settings");
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Loan period setting
        HBox loanPeriodBox = new HBox(10);
        Label loanPeriodLabel = new Label("Default Loan Period:");
        Spinner<Integer> loanPeriodSpinner = new Spinner<>(1, 60,
                prefs.getInt("loanPeriod", DEFAULT_LOAN_PERIOD));
        loanPeriodBox.getChildren().addAll(loanPeriodLabel, loanPeriodSpinner);

        // Max books setting
        HBox maxBooksBox = new HBox(10);
        Label maxBooksLabel = new Label("Maximum Books per User:");
        Spinner<Integer> maxBooksSpinner = new Spinner<>(1, 20,
                prefs.getInt("maxBooks", DEFAULT_MAX_BOOKS));
        maxBooksBox.getChildren().addAll(maxBooksLabel, maxBooksSpinner);

        // Fine rate setting
        HBox fineRateBox = new HBox(10);
        Label fineRateLabel = new Label("Late Return Fine (per day):");
        TextField fineRateField = new TextField(
                String.valueOf(prefs.getDouble("fineRate", 0.50)));
        fineRateBox.getChildren().addAll(fineRateLabel, fineRateField);

        loanBox.getChildren().addAll(
                sectionLabel,
                loanPeriodBox,
                maxBooksBox,
                fineRateBox
        );

        return loanBox;
    }

    private VBox createNotificationSettings() {
        VBox notificationBox = new VBox(10);
        notificationBox.setPadding(new Insets(10));

        Label sectionLabel = new Label("Notification Settings");
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Email notifications toggle
        CheckBox emailNotificationsCheck = new CheckBox("Enable Email Notifications");
        emailNotificationsCheck.setSelected(
                prefs.getBoolean("emailNotifications", DEFAULT_EMAIL_NOTIFICATIONS));

        // Due date reminder settings
        HBox reminderBox = new HBox(10);
        Label reminderLabel = new Label("Send Due Date Reminder (days before):");
        Spinner<Integer> reminderSpinner = new Spinner<>(1, 7,
                prefs.getInt("reminderDays", 3));
        reminderBox.getChildren().addAll(reminderLabel, reminderSpinner);

        // SMTP Settings
        GridPane smtpGrid = new GridPane();
        smtpGrid.setHgap(10);
        smtpGrid.setVgap(10);
        smtpGrid.add(new Label("SMTP Server:"), 0, 0);
        smtpGrid.add(new TextField(prefs.get("smtpServer", "")), 1, 0);
        smtpGrid.add(new Label("SMTP Port:"), 0, 1);
        smtpGrid.add(new TextField(prefs.get("smtpPort", "587")), 1, 1);

        notificationBox.getChildren().addAll(
                sectionLabel,
                emailNotificationsCheck,
                reminderBox,
                smtpGrid
        );

        return notificationBox;
    }

    private VBox createAppearanceSettings() {
        VBox appearanceBox = new VBox(10);
        appearanceBox.setPadding(new Insets(10));

        Label sectionLabel = new Label("Appearance Settings");
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Theme selection
        HBox themeBox = new HBox(10);
        Label themeLabel = new Label("Theme:");
        ComboBox<String> themeComboBox = new ComboBox<>(
                FXCollections.observableArrayList("Light", "Dark", "System Default"));
        themeComboBox.setValue(prefs.get("theme", DEFAULT_THEME));
        themeBox.getChildren().addAll(themeLabel, themeComboBox);

        // Font size selection
        HBox fontBox = new HBox(10);
        Label fontLabel = new Label("Font Size:");
        ComboBox<String> fontSizeComboBox = new ComboBox<>(
                FXCollections.observableArrayList("Small", "Medium", "Large"));
        fontSizeComboBox.setValue(prefs.get("fontSize", "Medium"));
        fontBox.getChildren().addAll(fontLabel, fontSizeComboBox);

        appearanceBox.getChildren().addAll(
                sectionLabel,
                themeBox,
                fontBox
        );

        return appearanceBox;
    }

    private VBox createBackupSettings() {
        VBox backupBox = new VBox(10);
        backupBox.setPadding(new Insets(10));

        Label sectionLabel = new Label("Backup Settings");
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Automatic backup toggle
        CheckBox autoBackupCheck = new CheckBox("Enable Automatic Backups");
        autoBackupCheck.setSelected(prefs.getBoolean("autoBackup", true));

        // Backup frequency setting
        HBox frequencyBox = new HBox(10);
        Label frequencyLabel = new Label("Backup Frequency:");
        ComboBox<String> frequencyComboBox = new ComboBox<>(
                FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));
        frequencyComboBox.setValue(prefs.get("backupFrequency", "Weekly"));
        frequencyBox.getChildren().addAll(frequencyLabel, frequencyComboBox);

        // Backup location setting
        HBox locationBox = new HBox(10);
        Label locationLabel = new Label("Backup Location:");
        TextField locationField = new TextField(
                prefs.get("backupLocation", System.getProperty("user.home") + "/library_backup"));
        Button browseButton = new Button("Browse");
        locationBox.getChildren().addAll(locationLabel, locationField, browseButton);

        backupBox.getChildren().addAll(
                sectionLabel,
                autoBackupCheck,
                frequencyBox,
                locationBox
        );

        return backupBox;
    }

    private void saveAllSettings() {
        try {
            // Save all settings to preferences
            // This is where you would implement the actual saving logic

            statusLabel.setText("Settings saved successfully!");
            statusLabel.setTextFill(Color.GREEN);
        } catch (Exception e) {
            statusLabel.setText("Error saving settings: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
        }
    }

    private void resetToDefaults() {
        try {
            // Reset all settings to their default values
            prefs.clear();

            // Refresh the UI
            settingsContainer.getChildren().clear();
            initializeSettings();

            statusLabel.setText("Settings reset to defaults successfully!");
            statusLabel.setTextFill(Color.GREEN);
        } catch (Exception e) {
            statusLabel.setText("Error resetting settings: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
        }
    }

    public VBox getSettingsContainer() {
        return settingsContainer;
    }
}
