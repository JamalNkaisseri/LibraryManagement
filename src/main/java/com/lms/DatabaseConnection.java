package com.lms;

import java.sql.*;

/**
 * The DatabaseConnection class is responsible for establishing a connection to the MySQL database.
 * It provides methods to test the connection, retrieve a database connection, and authenticate users.
 */
public class DatabaseConnection {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "KaiHavertz29#"; // Update this based on your actual DB credentials

    /**
     * Tests the connection to the database by attempting to establish a connection.
     */
    public void testConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL JDBC driver
            try (Connection connection = getConnection()) { // Attempt to get a connection
                if (connection != null) {
                    System.out.println("Connected to the database successfully!"); // Print success message
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace(); // Print error details
        }
    }

    /**
     * Retrieves a connection to the database.
     *
     * @return A Connection object to interact with the database.
     * @throws SQLException if a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Return the connection object
    }

    /**
     * Authenticates the user by checking the provided username and password with the database.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return true if the username and password match, false otherwise.
     */
    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // If a record is found, authentication is successful
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the category ID based on the category name.
     *
     * @param categoryName The name of the category.
     * @return The category ID, or -1 if the category does not exist.
     */
    public int getCategoryId(String categoryName) {
        String sql = "SELECT category_id FROM category WHERE name = ?"; // Ensure this matches your database schema

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName); // Set the category name parameter
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id"); // Return the category ID
                } else {
                    System.out.println("Category not found.");
                    return -1; // Return -1 if the category doesn't exist
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching category ID: " + e.getMessage());
            return -1; // Return -1 if there was an error
        }
    }
}
