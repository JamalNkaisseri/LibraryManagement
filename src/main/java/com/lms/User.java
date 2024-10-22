package com.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private static final DatabaseConnection dbConnection = new DatabaseConnection(); // Create a single instance of DatabaseConnection
    private final String username;
    private final String password; // Store the raw password temporarily

    // Constructor for new user registration
    public User(String username, String password) {
        this.username = username;
        this.password = password; // Store the raw password for now
    }

    // Method to register a new user
    public boolean register() {
        try {
            if (usernameExists(username)) {
                return false; // Username already exists
            }

            String hashedPassword = hashPassword(password);
            return saveUserToDatabase(username, hashedPassword);
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false; // Registration failed
        }
    }

    // Method to check if a username already exists in the database
    private boolean usernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection(); // Use the dbConnection instance to get the connection
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Return true if count is greater than 0
            }
        }
        return false; // Return false if any error occurs or if no results
    }

    // Method to save a new user to the database
    private boolean saveUserToDatabase(String username, String hashedPassword) throws SQLException {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = dbConnection.getConnection(); // Use the dbConnection instance to get the connection
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true; // Registration successful
        }
    }

    // Method to hash the password
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] byteData = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : byteData) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Method to log in a user
    public boolean login(String inputUsername, String inputPassword) {
        try {
            // Hash the input password to compare with the stored hashed password
            String hashedInputPassword = hashPassword(inputPassword);
            String query = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, inputUsername);
                pstmt.setString(2, hashedInputPassword);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) > 0; // Return true if a matching user is found
                }
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false; // Return false if login fails
    }

    // Additional methods for other user-related operations can go here
}
