package com.lms; // Package declaration for organizing related classes

import java.sql.Connection; // Import for SQL Connection
import java.sql.PreparedStatement; // Import for prepared statements
import java.sql.ResultSet; // Import for SQL result sets
import java.sql.SQLException; // Import for SQL exceptions
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

    // Constructor
    public Book(String title, String author, String isbn, int categoryId) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryId = categoryId; // Initialize categoryId
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
        String sql = "INSERT INTO books (title, author, isbn, category_id) VALUES (?, ?, ?, ?)";
        DatabaseConnection dbConnection = new DatabaseConnection(); // Create instance of DatabaseConnection

        try (Connection conn = dbConnection.getConnection(); // Get connection using DatabaseConnection
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the values for the prepared statement
            stmt.setString(1, this.title);
            stmt.setString(2, this.author);
            stmt.setString(3, this.isbn);
            stmt.setInt(4, this.categoryId); // Set the category ID

            // Execute the insertion and return true if successful
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error adding book to the database: " + e.getMessage());
            return false;
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
                Book book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("category_id"));
                book.setId(rs.getInt("book_id")); // Set the ID for the Book object
                books.add(book); // Add the Book object to the list
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving books from the database: " + e.getMessage());
        }

        return books; // Return the list of books
    }
}
