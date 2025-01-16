package com.librarymanagement.gui;

import com.lms.Book;
import com.lms.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class UserDashboard {
    private final User currentUser;
    private TableView<Book> bookTable;
    private TableView<Book> borrowedBooksTable;
    private Label statusLabel;
    private TextField searchField;
    private FilteredList<Book> filteredBooks;

    public UserDashboard(User user) {
        this.currentUser = user;
        this.statusLabel = new Label();
        this.statusLabel.setStyle("-fx-text-fill: green;");
    }

    public Scene createUserDashboard(Stage primaryStage, LibraryApp libraryApp) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // Create the top section with search and user info
        VBox topSection = createTopSection(primaryStage, libraryApp);
        mainLayout.setTop(topSection);

        // Create tabs for available books and borrowed books
        TabPane tabPane = new TabPane();

        // Available Books Tab
        Tab availableBooksTab = new Tab("Available Books");
        availableBooksTab.setContent(createAvailableBooksSection());
        availableBooksTab.setClosable(false);

        // Borrowed Books Tab
        Tab borrowedBooksTab = new Tab("My Borrowed Books");
        borrowedBooksTab.setContent(createBorrowedBooksSection());
        borrowedBooksTab.setClosable(false);

        tabPane.getTabs().addAll(availableBooksTab, borrowedBooksTab);
        mainLayout.setCenter(tabPane);

        // Status label at the bottom
        mainLayout.setBottom(statusLabel);

        return new Scene(mainLayout, 1000, 600);
    }

    private VBox createTopSection(Stage primaryStage, LibraryApp libraryApp) {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(10));

        // User information
        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Search section
        HBox searchBox = new HBox(10);
        searchField = new TextField();
        searchField.setPromptText("Search books by title or author...");
        searchField.setPrefWidth(300);

        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredBooks != null) {
                filteredBooks.setPredicate(book -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return book.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                            book.getAuthor().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.showLoginPage(primaryStage, libraryApp);
        });

        searchBox.getChildren().addAll(new Label("Search:"), searchField, logoutButton);
        topSection.getChildren().addAll(welcomeLabel, searchBox);

        return topSection;
    }

    private VBox createAvailableBooksSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        // Create table
        bookTable = new TableView<>();
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create columns
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

        TableColumn<Book, Integer> availableCol = new TableColumn<>("Available Copies");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));

        bookTable.getColumns().addAll(titleCol, authorCol, isbnCol, categoryCol, availableCol);

        // Borrow button
        Button borrowButton = new Button("Borrow Selected Book");
        borrowButton.setOnAction(e -> handleBorrowBook());

        // Load initial data
        refreshBookTable();

        section.getChildren().addAll(bookTable, borrowButton);
        return section;
    }

    private VBox createBorrowedBooksSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        // Create borrowed books table
        borrowedBooksTable = new TableView<>();
        borrowedBooksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create columns
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        TableColumn<Book, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        borrowedBooksTable.getColumns().addAll(titleCol, authorCol, isbnCol, dueDateCol);

        // Return button
        Button returnButton = new Button("Return Selected Book");
        returnButton.setOnAction(e -> handleReturnBook());

        // Load borrowed books
        refreshBorrowedBooksTable();

        section.getChildren().addAll(borrowedBooksTable, returnButton);
        return section;
    }

    private void handleBorrowBook() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            if (selectedBook.getAvailableCopies() > 0) {
                if (selectedBook.borrowBook(currentUser.getUsername())) {
                    refreshBookTable();
                    refreshBorrowedBooksTable();
                    statusLabel.setText("Book borrowed successfully!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                } else {
                    statusLabel.setText("Failed to borrow book.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            } else {
                statusLabel.setText("No copies available for borrowing.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            statusLabel.setText("Please select a book to borrow.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleReturnBook() {
        Book selectedBook = borrowedBooksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            if (selectedBook.returnBook(currentUser.getUsername())) {
                refreshBookTable();
                refreshBorrowedBooksTable();
                statusLabel.setText("Book returned successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Failed to return book.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            statusLabel.setText("Please select a book to return.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void refreshBookTable() {
        List<Book> books = Book.viewAllBooks();
        ObservableList<Book> bookData = FXCollections.observableArrayList(books);
        filteredBooks = new FilteredList<>(bookData, p -> true);
        bookTable.setItems(filteredBooks);
    }

    private void refreshBorrowedBooksTable() {
        List<Book> borrowedBooks = Book.viewBorrowedBooks(currentUser.getUsername());
        borrowedBooksTable.setItems(FXCollections.observableArrayList(borrowedBooks));
    }
}