package com.lms; // Package declaration for organizing related classes

import java.sql.Connection; // Import for SQL Connection
import java.sql.DriverManager; // Import for managing database connections
import java.sql.SQLException; // Import for SQL exceptions

/**
 * The DatabaseConnection class is responsible for establishing a connection to the MySQL database.
 * It provides methods to test the connection and retrieve a database connection.
 */
public class DatabaseConnection {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library?useSSL=false&serverTimezone=UTC"; // URL of the database
    private static final String DB_USER = "root"; // Database username
    private static final String DB_PASSWORD = "KaiHavertz29#"; // Database password

    /**
     * Tests the connection to the database by attempting to establish a connection.
     */
    public void testConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load the MySQL JDBC driver
            try (Connection connection = getConnection()) { // Attempt to get a connection using the getConnection method
                if (connection != null) { // Check if the connection was successful
                    System.out.println("Connected to the database successfully!"); // Print success message
                }
            }
        } catch (ClassNotFoundException e) {
            // Exception handling for when the JDBC driver is not found
            System.out.println("MySQL JDBC Driver not found."); // Print error message
            e.printStackTrace(); // Print stack trace for debugging
        } catch (SQLException e) {
            // Exception handling for SQL-related errors
            System.out.println("Failed to connect to the database."); // Print error message
            System.out.println("Error: " + e.getMessage()); // Print the error message
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    /**
     * Retrieves a connection to the database.
     *
     * @return A Connection object to interact with the database.
     * @throws SQLException if a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Create and return a new connection using the provided credentials
    }
}
