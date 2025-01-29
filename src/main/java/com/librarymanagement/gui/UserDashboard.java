package com.librarymanagement.gui;

import com.lms.Book;
import com.lms.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

public class UserDashboard {
    private final User currentUser;
    private TableView<Book> bookTable;
    private TableView<Book> borrowedBooksTable;
    private Label statusLabel;
    private TextField searchField;
    private FilteredList<Book> filteredBooks;
    private StackPane contentArea;
    private HBox searchBox; // New field to hold the search container

    public UserDashboard(User user) {
        this.currentUser = user;
        this.statusLabel = new Label();
        this.statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
        this.contentArea = new StackPane();
    }

    public Scene createUserDashboard(Stage primaryStage, LibraryApp libraryApp) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f4f6f9;");
        mainLayout.setPadding(new Insets(20));

        HBox topSection = createTopSection(primaryStage, libraryApp);
        mainLayout.setTop(topSection);

        VBox leftPanel = createLeftPanel(primaryStage, libraryApp);

        contentArea.setPadding(new Insets(20));
        contentArea.getChildren().add(createAvailableBooksSection());

        mainLayout.setLeft(leftPanel);
        mainLayout.setCenter(contentArea);

        HBox bottomBox = new HBox(statusLabel);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        mainLayout.setBottom(bottomBox);

        Scene scene = new Scene(mainLayout, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        return scene;
    }

    private VBox createLeftPanel(Stage primaryStage, LibraryApp libraryApp) {
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.getStyleClass().add("left-panel");

        Hyperlink availableBooksLink = new Hyperlink("Available Books");
        availableBooksLink.getStyleClass().add("menu-item");
        availableBooksLink.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createAvailableBooksSection());
            searchField.setVisible(true); // Show search when viewing available books
            searchBox.setVisible(true);
        });

        Hyperlink borrowedBooksLink = new Hyperlink("My Borrowed Books");
        borrowedBooksLink.getStyleClass().add("menu-item");
        borrowedBooksLink.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createBorrowedBooksSection());
            searchField.setVisible(false); // Hide search when viewing borrowed books
            searchBox.setVisible(false);
        });

        Hyperlink profileLink = new Hyperlink("Profile");
        profileLink.getStyleClass().add("profile-link");
        profileLink.setOnAction(e -> {
            // Implement Profile page display logic
        });

        leftPanel.getChildren().addAll(availableBooksLink, borrowedBooksLink, profileLink);

        return leftPanel;
    }

    private HBox createTopSection(Stage primaryStage, LibraryApp libraryApp) {
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setPadding(new Insets(0, 0, 20, 0));

        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Create search field and its container
        searchField = new TextField();
        searchField.setPromptText("🔍 Search books by title or author...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(350);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredBooks != null) {
                filteredBooks.setPredicate(book -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();
                    return book.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                            book.getAuthor().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });

        // Create a container for the search field
        searchBox = new HBox(searchField);
        searchBox.setAlignment(Pos.CENTER);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.showLoginPage(primaryStage, libraryApp);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topSection.getChildren().addAll(welcomeLabel, spacer, searchBox, logoutButton);

        return topSection;
    }

    private VBox createAvailableBooksSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        bookTable = new TableView<>();
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        Button borrowButton = new Button("Borrow Selected Book");
        borrowButton.getStyleClass().add("borrow-button");
        borrowButton.setOnAction(e -> handleBorrowBook());

        refreshBookTable();

        section.getChildren().addAll(bookTable, borrowButton);
        return section;
    }

    private VBox createBorrowedBooksSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        borrowedBooksTable = new TableView<>();
        borrowedBooksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        TableColumn<Book, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        borrowedBooksTable.getColumns().addAll(titleCol, authorCol, isbnCol, dueDateCol);

        refreshBorrowedBooksTable();

        section.getChildren().addAll(borrowedBooksTable);
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