package com.lms; // Package declaration for organizing related classes

import java.sql.*;
import java.util.ArrayList; // Import for ArrayList
import java.util.List; // Import for List

/*
Book Categories
  1.Fiction
  2.Non-Fiction
  3.Science Fiction
  4.Biography
 */

/**
 * The Book class represents a book entity and contains methods for
 * interacting with the books table in the database.
 */
public class Book {
    private int id; // Book ID
    private String title; // Book title
    private String author; // Author of the book
    private String isbn; // ISBN of the book
    private int categoryId; // Category ID
    private int totalCopies; //Copies of the book

    // Constructor
    public Book(String title, String author, String isbn, int categoryId, int totalCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryId = categoryId; // Initialize categoryId
        this.totalCopies = totalCopies;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Adds the book to the database.
     *
     * @return true if the book was added successfully, false otherwise.
     */

    public boolean addBookToDatabase() {
        // SQL query to insert a new book
        String insertBookSql = "INSERT INTO books (title, author, isbn, category_id, total_copies) VALUES (?, ?, ?, ?, ?)";

        // SQL query to insert copies for the newly added book
        String insertCopiesSql = "INSERT INTO copies (book_id, barcode, status, `condition`) "
                + "SELECT ?, CONCAT('BC', ?, '-', numbers.dummy), 'available', 'new' "
                + "FROM (SELECT 1 AS dummy UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS numbers "
                + "WHERE numbers.dummy <= ?";

        DatabaseConnection dbConnection = new DatabaseConnection(); // Create instance of DatabaseConnection

        try (Connection conn = dbConnection.getConnection(); // Get connection using DatabaseConnection
             PreparedStatement bookStmt = conn.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {

            // Set the values for the prepared statement to insert the book
            bookStmt.setString(1, this.title);
            bookStmt.setString(2, this.author);
            bookStmt.setString(3, this.isbn);
            bookStmt.setInt(4, this.categoryId); // Set the category ID
            bookStmt.setInt(5, this.totalCopies); // Set total copies for the new book

            // Execute the insertion to add the book
            int rowsAffected = bookStmt.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve the generated book ID
                ResultSet generatedKeys = bookStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int bookId = generatedKeys.getInt(1); // Get the book_id of the newly added book

                    // Insert copies for the new book
                    try (PreparedStatement copiesStmt = conn.prepareStatement(insertCopiesSql)) {
                        copiesStmt.setInt(1, bookId); // Set book_id for the copies
                        copiesStmt.setInt(2, bookId); // Use the same book_id to generate the barcode
                        copiesStmt.setInt(3, this.totalCopies); // Set the number of copies to insert

                        // Execute the insertion of copies
                        copiesStmt.executeUpdate();
                    }
                }
                return true; // Book and copies successfully added
            } else {
                return false; // No rows were affected, meaning the insertion failed
            }

        } catch (SQLException e) {
            System.out.println("Error adding book and copies to the database: " + e.getMessage());
            return false; // Return false if there was an error
        }
    }



    /**
     * Updates the book's title and/or author in the database.
     *
     * @param newTitle The new title of the book, or null to keep the existing title.
     * @param newAuthor The new author of the book, or null to keep the existing author.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateBook(String newTitle, String newAuthor) {
        StringBuilder sql = new StringBuilder("UPDATE books SET ");
        boolean isFirst = true;

        // Append the new title if provided
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            sql.append("title = ?");
            isFirst = false;
        }

        // Append the new author if provided
        if (newAuthor != null && !newAuthor.trim().isEmpty()) {
            if (!isFirst) {
                sql.append(", "); // Add comma if we already have a title
            }
            sql.append("author = ?");
        }

        // Complete the SQL statement with a WHERE clause
        sql.append(" WHERE book_id = ?");

        DatabaseConnection dbConnection = new DatabaseConnection(); // Create instance of DatabaseConnection

        try (Connection conn = dbConnection.getConnection(); // Get connection using DatabaseConnection
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            // Set the parameters based on the input
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                stmt.setString(paramIndex++, newTitle);
            }

            if (newAuthor != null && !newAuthor.trim().isEmpty()) {
                stmt.setString(paramIndex++, newAuthor);
            }

            // Set the book ID for the WHERE clause
            stmt.setInt(paramIndex, this.id);

            // Execute the update and return true if successful
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating book in the database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the book from the database based on the book ID.
     *
     * @return true if the book was deleted successfully, false otherwise.
     */
    public boolean deleteBook() {
        String sql = "DELETE FROM books WHERE book_id = ?";
        DatabaseConnection dbConnection = new DatabaseConnection(); // Create instance of DatabaseConnection

        try (Connection conn = dbConnection.getConnection(); // Get connection using DatabaseConnection
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the book ID for the WHERE clause
            stmt.setInt(1, this.id);

            // Execute the delete and return true if successful
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting book from the database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all books from the database.
     *
     * @return A list of Book objects representing all books in the database.
     */
    public static List<Book> viewAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, isbn, category_id FROM books";
        DatabaseConnection dbConnection = new DatabaseConnection(); // Create instance of DatabaseConnection

        try (Connection conn = dbConnection.getConnection(); // Get connection using DatabaseConnection
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Process the result set
            while (rs.next()) {
                Book book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("category_id"), rs.getInt("total_copies"));
                book.setId(rs.getInt("book_id")); // Set the ID for the Book object
                books.add(book); // Add the Book object to the list
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving books from the database: " + e.getMessage());
        }

        return books; // Return the list of books
    }
}
