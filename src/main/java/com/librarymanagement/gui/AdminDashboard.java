package com.librarymanagement.gui;

import com.lms.Book;
import com.lms.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class AdminDashboard {
    private TableView<Book> bookTable;
    private TextField titleField, authorField, isbnField, copiesField;
    private ComboBox<String> categoryComboBox;
    private Label statusLabel;
    private User currentUser;

    // Constructor accepts User object to initialize currentUser and statusLabel
    public AdminDashboard(User user) {
        this.currentUser = user;
        this.statusLabel = new Label(); // Initialize the statusLabel here
        this.statusLabel.setStyle("-fx-text-fill: green;"); // Set default text color or style
    }

    // Updated method without requiring currentUser to be passed again
    public Scene createAdminDashboard(Stage primaryStage) {
        if (!currentUser.getRole().equalsIgnoreCase("admin")) {
            statusLabel.setText("Access Denied: You must be an admin to view this page.");
            return new Scene(new VBox(statusLabel), 400, 200); // Simple access denied message for non-admin users
        }

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // Create the book input form
        VBox inputForm = createInputForm();
        mainLayout.setLeft(inputForm);

        // Create the book table
        createBookTable();
        mainLayout.setCenter(bookTable);

        // Load initial data
        refreshBookTable();

        return new Scene(mainLayout, 1000, 600);
    }

    private VBox createInputForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(10));
        form.setMinWidth(300);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        // Create form fields
        titleField = new TextField();
        authorField = new TextField();
        isbnField = new TextField();
        copiesField = new TextField();
        categoryComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Fiction", "Non-Fiction", "Science Fiction", "Biography"
        ));

        // Add labels and fields to grid
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

        // Create buttons
        Button addButton = new Button("Add Book");
        Button deleteButton = new Button("Delete Selected");
        Button refreshButton = new Button("Refresh Table");

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addButton, deleteButton, refreshButton);

        // Status label for feedback
        // statusLabel is initialized in the constructor already

        // Add event handlers
        addButton.setOnAction(e -> handleAddBook());
        deleteButton.setOnAction(e -> handleDeleteBook());
        refreshButton.setOnAction(e -> refreshBookTable());

        // Compose the form
        form.getChildren().addAll(
                new Label("Add New Book"),
                grid,
                buttonBox,
                statusLabel
        );

        return form;
    }

    private void createBookTable() {
        bookTable = new TableView<>();
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create columns using PropertyValueFactory
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

        bookTable.getColumns().addAll(titleCol, authorCol, isbnCol, categoryCol);
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

            // Convert category string to ID
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
}
