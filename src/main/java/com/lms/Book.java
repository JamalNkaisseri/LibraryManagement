package com.lms;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File; // Added missing import for File class

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private int categoryId;
    private int totalCopies;
    private Date loanDate;
    private Date dueDate;
    private String pdfFilePath;

    // Constructor
    public Book(String title, String author, String isbn, int categoryId, int totalCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.totalCopies = totalCopies;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public int getTotalCopies() { return totalCopies; }
    public Date getLoanDate() { return loanDate; }
    public void setLoanDate(Date loanDate) { this.loanDate = loanDate; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public String getPdfFilePath() { return pdfFilePath; }
    public void setPdfFilePath(String pdfFilePath) { this.pdfFilePath = pdfFilePath; }

    // Get available copies count
    public int getAvailableCopies() {
        String sql = """
            SELECT 
                COALESCE(
                    (SELECT COUNT(*) 
                     FROM copies 
                     WHERE book_id = ? AND status = 'available'),
                    0
                ) as available,
                b.total_copies
            FROM books b
            WHERE b.book_id = ?
        """;

        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);
            stmt.setInt(2, this.id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("available");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving available copies: " + e.getMessage());
        }
        return 0;
    }

    // Add new book to database
    public boolean addBookToDatabase() {
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // First insert the book
            String bookSql = "INSERT INTO books (title, author, isbn, category_id, total_copies, pdf_file_path) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(bookSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, this.title);
                stmt.setString(2, this.author);
                stmt.setString(3, this.isbn);
                stmt.setInt(4, this.categoryId);
                stmt.setInt(5, this.totalCopies);
                stmt.setString(6, this.pdfFilePath); // Added PDF path to the insert

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Get the generated book_id
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int bookId = rs.getInt(1);
                        this.id = bookId; // Set the ID of the current object

                        // Insert individual copies with barcode and condition
                        String copySql = "INSERT INTO copies (book_id, barcode, status, `condition`) VALUES (?, ?, 'available', 'New')";
                        try (PreparedStatement copyStmt = conn.prepareStatement(copySql)) {
                            for (int i = 0; i < this.totalCopies; i++) {
                                copyStmt.setInt(1, bookId);
                                String barcode = this.isbn + String.format("%03d", i + 1);
                                copyStmt.setString(2, barcode);
                                copyStmt.addBatch();
                            }
                            copyStmt.executeBatch();
                        }

                        conn.commit();
                        return true;
                    }
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.out.println("Error adding book to database: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Borrow a book
    public boolean borrowBook(String username) {
        String borrowSql = """
            INSERT INTO loan (copy_id, user_id, loan_date, due_date, status)
            SELECT c.copy_id, u.id, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'borrowed'
            FROM copies c
            JOIN users u ON u.username = ?
            WHERE c.book_id = ? AND c.status = 'available'
            LIMIT 1
        """;

        String updateCopySql = """
            UPDATE copies 
            SET status = 'borrowed'
            WHERE copy_id = (
                SELECT copy_id 
                FROM loan 
                WHERE status = 'borrowed' 
                ORDER BY loan_date DESC 
                LIMIT 1
            )
        """;

        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert loan record
            try (PreparedStatement stmt = conn.prepareStatement(borrowSql)) {
                stmt.setString(1, username);
                stmt.setInt(2, this.id);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Update copy status
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateCopySql)) {
                        updateStmt.executeUpdate();
                    }
                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.out.println("Error borrowing book: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Return a book
    public boolean returnBook(String username) {
        String returnLoanSql = """
            UPDATE loan 
            SET status = 'returned', return_date = CURDATE()
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
            AND copy_id IN (SELECT copy_id FROM copies WHERE book_id = ?)
            AND status = 'borrowed' 
            AND return_date IS NULL
        """;

        String returnCopySql = """
            UPDATE copies 
            SET status = 'available'
            WHERE book_id = ? 
            AND copy_id IN (
                SELECT copy_id 
                FROM loan 
                WHERE status = 'returned'
                AND return_date = CURDATE()
            )
        """;

        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(returnLoanSql)) {
                stmt1.setString(1, username);
                stmt1.setInt(2, this.id);
                int rowsAffectedLoan = stmt1.executeUpdate();

                if (rowsAffectedLoan > 0) {
                    try (PreparedStatement stmt2 = conn.prepareStatement(returnCopySql)) {
                        stmt2.setInt(1, this.id);
                        stmt2.executeUpdate();
                        conn.commit();
                        return true;
                    }
                }
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.out.println("Error returning book: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // View all borrowed books for a user
    public static List<Book> viewBorrowedBooks(String username) {
        List<Book> borrowedBooks = new ArrayList<>();
        String sql = """
            SELECT b.book_id, b.title, b.author, b.isbn, b.category_id, b.total_copies,
                   l.loan_date, l.due_date, b.pdf_file_path
            FROM books b
            JOIN copies c ON b.book_id = c.book_id
            JOIN loan l ON c.copy_id = l.copy_id
            WHERE l.user_id = (SELECT id FROM users WHERE username = ?)
            AND l.status = 'borrowed'
        """;

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
                book.setLoanDate(rs.getDate("loan_date"));
                book.setDueDate(rs.getDate("due_date"));
                book.setPdfFilePath(rs.getString("pdf_file_path"));
                borrowedBooks.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving borrowed books: " + e.getMessage());
        }
        return borrowedBooks;
    }

    // View all books in the library
    public static List<Book> viewAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, isbn, category_id, total_copies, pdf_file_path FROM books";

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
                book.setPdfFilePath(rs.getString("pdf_file_path"));
                books.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving books from the database: " + e.getMessage());
        }
        return books;
    }

    // Delete a book
    public boolean deleteBook() {
        String deleteBookSql = "DELETE FROM books WHERE book_id = ?";
        DatabaseConnection dbConnection = new DatabaseConnection();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteBookSql)) {
            stmt.setInt(1, this.id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting book from database: " + e.getMessage());
            return false;
        }
    }

    // Method to save PDF path to database
    public boolean savePDFPath() {
        String sql = "UPDATE books SET pdf_file_path = ? WHERE book_id = ?";

        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, this.pdfFilePath);
            stmt.setInt(2, this.id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error saving PDF path: " + e.getMessage());
            return false;
        }
    }

    // Method to load PDF path from database
    public boolean loadPDFPath() {
        String sql = "SELECT pdf_file_path FROM books WHERE book_id = ?";

        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.pdfFilePath = rs.getString("pdf_file_path");
                return this.pdfFilePath != null && !this.pdfFilePath.isEmpty();
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error loading PDF path: " + e.getMessage());
            return false;
        }
    }

    // Check if PDF file exists
    public boolean hasPDF() {
        if (pdfFilePath == null || pdfFilePath.isEmpty()) {
            if (!loadPDFPath()) {
                return false;
            }
        }

        if (pdfFilePath != null && !pdfFilePath.isEmpty()) {
            File file = new File(pdfFilePath);
            return file.exists() && file.isFile();
        }
        return false;
    }

    // Static method to get a book with its PDF path
    public static Book getBookWithPDF(int bookId) {
        String sql = "SELECT book_id, title, author, isbn, category_id, total_copies, pdf_file_path FROM books WHERE book_id = ?";

        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Book book = new Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("category_id"),
                        rs.getInt("total_copies")
                );
                book.setId(rs.getInt("book_id"));
                book.setPdfFilePath(rs.getString("pdf_file_path"));
                return book;
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving book with PDF: " + e.getMessage());
        }
        return null;
    }
}