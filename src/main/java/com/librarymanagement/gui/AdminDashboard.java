package com.librarymanagement.gui;

import com.lms.Book;
import com.lms.User;
import com.lms.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboard {
    private TableView<Book> bookTable;
    private TextField titleField, authorField, isbnField, copiesField;
    private ComboBox<String> categoryComboBox;
    private Label statusLabel;
    private User currentUser;
    private StackPane contentArea;
    private VBox bookManagementForm;
    private TextField usernameField;
    private TextField passwordField;
    private ComboBox<String> roleComboBox;
    private TableView<User> userTable;
    private VBox userManagementForm;

    private UserManager userManager = new UserManager();

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.statusLabel = new Label();
        this.statusLabel.setStyle("-fx-text-fill: green;");
        this.contentArea = new StackPane();
        initializeBookManagementForm();
        initializeUserManagementForm();
    }

    public Scene createAdminDashboard(Stage primaryStage, LibraryApp libraryApp) {
        if (!currentUser.getRole().equalsIgnoreCase("admin")) {
            statusLabel.setText("Access Denied: You must be an admin to view this page.");
            return new Scene(new VBox(statusLabel), 400, 200);
        }

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f4f6f9;");
        mainLayout.setPadding(new Insets(20));

        HBox topSection = createTopSection(primaryStage, libraryApp);
        mainLayout.setTop(topSection);

        VBox leftPanel = createLeftPanel();
        mainLayout.setLeft(leftPanel);

        contentArea.setPadding(new Insets(20));
        contentArea.getChildren().add(createBookManagementSection());
        mainLayout.setCenter(contentArea);

        HBox bottomBox = new HBox(statusLabel);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        mainLayout.setBottom(bottomBox);

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        return scene;
    }

    private HBox createTopSection(Stage primaryStage, LibraryApp libraryApp) {
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setPadding(new Insets(0, 0, 20, 0));

        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername() + " (Admin)");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.showLoginPage(primaryStage, libraryApp);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topSection.getChildren().addAll(welcomeLabel, spacer, logoutButton);

        return topSection;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.getStyleClass().add("left-panel");

        Hyperlink bookManagementLink = new Hyperlink("Book Management");
        bookManagementLink.getStyleClass().add("menu-item");
        bookManagementLink.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createBookManagementSection());
        });

        Hyperlink userManagementLink = new Hyperlink("User Management");
        userManagementLink.getStyleClass().add("menu-item");
        userManagementLink.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createUserManagementSection());
        });

        Hyperlink reportsLink = new Hyperlink("Reports & Analytics");
        reportsLink.getStyleClass().add("menu-item");
        reportsLink.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createReportsSection());
        });

        Hyperlink settingsLink = new Hyperlink("Settings");
        settingsLink.getStyleClass().add("menu-item");
        settingsLink.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createSettingsSection());
        });

        leftPanel.getChildren().addAll(
                bookManagementLink,
                userManagementLink,
                reportsLink,
                settingsLink
        );

        return leftPanel;
    }

    private void initializeBookManagementForm() {
        bookManagementForm = new VBox(10);
        bookManagementForm.setPadding(new Insets(10));
        bookManagementForm.setMinWidth(300);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        titleField = new TextField();
        authorField = new TextField();
        isbnField = new TextField();
        copiesField = new TextField();
        categoryComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Fiction", "Non-Fiction", "Science Fiction", "Biography"
        ));

        int row = 0;
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(new Label("Author:"), 0, row);
        grid.add(authorField, 1, row++);

        grid.add(new Label("ISBN:"), 0, row);
        grid.add(isbnField, 1, row++);

        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryComboBox, 1, row++);

        grid.add(new Label("Copies:"), 0, row);
        grid.add(copiesField, 1, row++);

        Button addButton = new Button("Add Book");
        Button deleteButton = new Button("Delete Selected");
        Button refreshButton = new Button("Refresh Table");

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, deleteButton, refreshButton);

        addButton.setOnAction(e -> handleAddBook());
        deleteButton.setOnAction(e -> handleDeleteBook());
        refreshButton.setOnAction(e -> refreshBookTable());

        bookManagementForm.getChildren().addAll(
                new Label("Add New Book"),
                grid,
                buttonBox
        );
    }


    private void createBookTable() {
        bookTable = new TableView<>();
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Set a minimum width and height for better visibility
        bookTable.setMinWidth(600);
        bookTable.setMinHeight(400);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        TableColumn<Book, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    setText(switch (book.getCategoryId()) {
                        case 1 -> "Fiction";
                        case 2 -> "Non-Fiction";
                        case 3 -> "Science Fiction";
                        case 4 -> "Biography";
                        default -> "Unknown";
                    });
                }
            }
        });

        TableColumn<Book, Integer> copiesCol = new TableColumn<>("Available Copies");
        copiesCol.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));

        bookTable.getColumns().addAll(titleCol, authorCol, isbnCol, categoryCol, copiesCol);
        refreshBookTable();
    }

    private VBox createBookManagementSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(10));

        createBookTable();

        HBox layout = new HBox(20);
        layout.getChildren().addAll(bookManagementForm, bookTable);

        section.getChildren().add(layout);
        return section;
    }

    private VBox createReportsSection() {
        try {
            ReportsAnalytics reports = new ReportsAnalytics();
            return reports.createReportsSection();
        } catch (Exception e) {
            VBox errorBox = new VBox(10);
            errorBox.setPadding(new Insets(20));
            Label errorLabel = new Label("Error loading reports: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            errorBox.getChildren().add(errorLabel);
            return errorBox;
        }
    }

    private VBox createSettingsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(10));

        Label placeholder = new Label("Settings Section - Coming Soon");
        placeholder.setStyle("-fx-font-size: 20px;");

        section.getChildren().add(placeholder);
        return section;
    }

    private void handleAddBook() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String isbn = isbnField.getText().trim();
            int copies = Integer.parseInt(copiesField.getText().trim());

            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || categoryComboBox.getValue() == null) {
                statusLabel.setText("Please fill in all fields");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            int categoryId = switch (categoryComboBox.getValue()) {
                case "Fiction" -> 1;
                case "Non-Fiction" -> 2;
                case "Science Fiction" -> 3;
                case "Biography" -> 4;
                default -> throw new IllegalArgumentException("Invalid category");
            };

            Book newBook = new Book(title, author, isbn, categoryId, copies);
            if (newBook.addBookToDatabase()) {
                clearFields();
                refreshBookTable();
                statusLabel.setText("Book added successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Failed to add book.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid number of copies");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleDeleteBook() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            if (selectedBook.deleteBook()) {
                refreshBookTable();
                statusLabel.setText("Book deleted successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Failed to delete book.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            statusLabel.setText("Please select a book to delete.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void refreshBookTable() {
        List<Book> books = Book.viewAllBooks();
        ObservableList<Book> bookData = FXCollections.observableArrayList(books);
        bookTable.setItems(bookData);
    }

    private void clearFields() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        copiesField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
    }

    private void refreshUserTable() {
        List<User> users = userManager.getAllUsers();
        ObservableList<User> userData = FXCollections.observableArrayList(users);
        userTable.setItems(userData);
    }


    private void initializeUserManagementForm() {
        // Initialize the class-level userManagementForm
        userManagementForm = new VBox(10);
        userManagementForm.setPadding(new Insets(10));
        userManagementForm.setMinWidth(300);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        // Initialize form fields
        usernameField = new TextField();
        passwordField = new TextField();
        roleComboBox = new ComboBox<>(FXCollections.observableArrayList("admin", "librarian", "member"));

        // Add form fields to the grid
        int row = 0;
        grid.add(new Label("Username:"), 0, row);
        grid.add(usernameField, 1, row++);


        grid.add(new Label("Password:"), 0, row);
        grid.add(passwordField, 1, row++);

        grid.add(new Label("Role:"), 0, row);
        grid.add(roleComboBox, 1, row++);

        // Buttons for user management actions
        Button addButton = new Button("Add User");
        Button deleteButton = new Button("Delete Selected");
        Button refreshButton = new Button("Refresh Table");

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, deleteButton, refreshButton);

        // Set button actions
        addButton.setOnAction(e -> handleAddUser());
        deleteButton.setOnAction(e -> handleDeleteUser());
        refreshButton.setOnAction(e -> refreshUserTable());

        // Add all components to the userManagementForm
        userManagementForm.getChildren().addAll(
                new Label("Add New User"),
                grid,
                buttonBox
        );
    }


    private void createUserTable() {
        userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        userTable.setMinWidth(600);
        userTable.setMinHeight(400);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        userTable.getColumns().addAll(usernameCol, emailCol, roleCol);
        refreshUserTable();
    }

    private VBox createUserManagementSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(10));

        // Initialize user table and form if not already done
        if (userTable == null) {
            createUserTable();
        }

        if (userManagementForm == null) {
            initializeUserManagementForm();
        }

        HBox layout = new HBox(20);
        layout.getChildren().addAll(userManagementForm, userTable);

        section.getChildren().add(layout);
        return section;
    }

    private void handleAddUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty() || role == null) {
            statusLabel.setText("Please fill in all fields");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (userManager.createUser(username, password, role)) {
            clearUserFields();
            refreshUserTable();
            statusLabel.setText("User added successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText("Failed to add user.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Prevent deleting the current admin user
            if (selectedUser.getUsername().equals(currentUser.getUsername())) {
                statusLabel.setText("Cannot delete the current logged-in user.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (userManager.deleteUser(selectedUser.getUsername())) {
                refreshUserTable();
                statusLabel.setText("User deleted successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Failed to delete user.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            statusLabel.setText("Please select a user to delete.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void clearUserFields() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
}