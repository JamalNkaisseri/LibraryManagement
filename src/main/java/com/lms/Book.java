package com.lms;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private int categoryId;
    private int totalCopies;

    // Constructor
    public Book(String title, String author, String isbn, int categoryId, int totalCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.totalCopies = totalCopies;
    }

    // Getters and setters
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

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        String sql = "SELECT COUNT(*) AS available FROM copies WHERE book_id = ? AND status = 'available'";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("available");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving available copies: " + e.getMessage());
        }
        return 0;
    }

    public boolean borrowBook(String username) {
        String borrowSql = "UPDATE copies SET status = 'borrowed', borrowed_by = ?, borrowed_date = NOW() WHERE book_id = ? AND status = 'available' LIMIT 1";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(borrowSql)) {
            stmt.setString(1, username);
            stmt.setInt(2, this.id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error borrowing book: " + e.getMessage());
            return false;
        }
    }

    public boolean returnBook(String username) {
        String returnSql = "UPDATE copies SET status = 'available', borrowed_by = NULL, borrowed_date = NULL WHERE book_id = ? AND borrowed_by = ? LIMIT 1";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(returnSql)) {
            stmt.setInt(1, this.id);
            stmt.setString(2, username);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error returning book: " + e.getMessage());
            return false;
        }
    }

    public static List<Book> viewBorrowedBooks(String username) {
        List<Book> borrowedBooks = new ArrayList<>();
        String sql = "SELECT b.book_id, b.title, b.author, b.isbn, b.category_id, b.total_copies, c.borrowed_date " +
                "FROM books b JOIN copies c ON b.book_id = c.book_id " +
                "WHERE c.borrowed_by = ? AND c.status = 'borrowed'";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Book book = new Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("category_id"),
                        rs.getInt("total_copies")
                );
                book.setId(rs.getInt("book_id"));
                borrowedBooks.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving borrowed books: " + e.getMessage());
        }
        return borrowedBooks;
    }

    public static List<Book> viewAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, isbn, category_id, total_copies FROM books";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Book book = new Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("category_id"),
                        rs.getInt("total_copies")
                );
                book.setId(rs.getInt("book_id"));
                books.add(book);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving books from the database: " + e.getMessage());
        }
        return books;
    }

    // Method to add a new book to the database
    public boolean addBookToDatabase() {
        String sql = "INSERT INTO books (title, author, isbn, category_id, total_copies) VALUES (?, ?, ?, ?, ?)";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.title);
            stmt.setString(2, this.author);
            stmt.setString(3, this.isbn);
            stmt.setInt(4, this.categoryId);
            stmt.setInt(5, this.totalCopies);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error adding book to database: " + e.getMessage());
            return false;
        }
    }

    // Method to delete a book from the database by its ID
    public boolean deleteBook() {
        String sql = "DELETE FROM books WHERE book_id = ?";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting book from database: " + e.getMessage());
            return false;
        }
    }
}
