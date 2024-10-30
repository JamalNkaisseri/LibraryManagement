package com.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The SearchService class provides methods to search for books
 * by title, author, and category in the library database.
 */
public class SearchService {

    /**
     * Searches for books by title.
     *
     * @param title The title of the book to search for.
     * @return A list of Book objects that match the given title.
     */
    public List<Book> searchByTitle(String title) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, isbn, category_id FROM books WHERE title LIKE ?";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the parameter for the prepared statement
            stmt.setString(1, "%" + title + "%");
            ResultSet rs = stmt.executeQuery();

            // Process the result set
            while (rs.next()) {
                Book book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("category_id"));
                book.setId(rs.getInt("book_id"));
                books.add(book);
            }

        } catch (SQLException e) {
            System.out.println("Error searching books by title: " + e.getMessage());
        }

        return books;
    }

    /**
     * Searches for books by author.
     *
     * @param author The author of the book to search for.
     * @return A list of Book objects that match the given author.
     */
    public List<Book> searchByAuthor(String author) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, isbn, category_id FROM books WHERE author LIKE ?";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the parameter for the prepared statement
            stmt.setString(1, "%" + author + "%");
            ResultSet rs = stmt.executeQuery();

            // Process the result set
            while (rs.next()) {
                Book book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("category_id"));
                book.setId(rs.getInt("book_id"));
                books.add(book);
            }

        } catch (SQLException e) {
            System.out.println("Error searching books by author: " + e.getMessage());
        }

        return books;
    }

    /**
     * Searches for books by category ID.
     *
     * @param categoryId The category ID of the books to search for.
     * @return A list of Book objects that match the given category ID.
     */
    public List<Book> searchByCategory(int categoryId) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, isbn, category_id FROM books WHERE category_id = ?";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the parameter for the prepared statement
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            // Process the result set
            while (rs.next()) {
                Book book = new Book(rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("category_id"));
                book.setId(rs.getInt("book_id"));
                books.add(book);
            }

        } catch (SQLException e) {
            System.out.println("Error searching books by category: " + e.getMessage());
        }

        return books;
    }

    /**
     * Displays all books under a certain category based on the category name provided by the user.
     */
    public void displayBooksByCategory() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the category name to search for books: ");
        String categoryName = scanner.nextLine();

        // Get the category ID based on the category name
        DatabaseConnection dbConnection = new DatabaseConnection();
        int categoryId = dbConnection.getCategoryId(categoryName);

        if (categoryId == -1) {
            System.out.println("Invalid category. Please try again.");
            return;
        }

        List<Book> books = searchByCategory(categoryId);

        // Display the search results
        if (books.isEmpty()) {
            System.out.println("No books found under this category.");
        } else {
            System.out.println("Books under the category \"" + categoryName + "\":");
            for (Book book : books) {
                System.out.println("ID: " + book.getId() + ", Title: " + book.getTitle() +
                        ", Author: " + book.getAuthor() + ", ISBN: " + book.getIsbn());
            }
        }

        scanner.close(); // Close the scanner if no further input is required
    }
}
