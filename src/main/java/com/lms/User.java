package com.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private static final DatabaseConnection dbConnection = new DatabaseConnection(); // Single instance of DatabaseConnection
    private final String username;
    private final String password; // Store the raw password temporarily
    private String role; // Store the user's role (e.g., "admin" or "user")

    // Constructor for new user registration
    public User(String username, String password) {
        this.username = username;
        this.password = password; // Store the raw password temporarily
    }

    // Constructor for an existing user (with role)
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Method to register a new user
    public boolean register(String role) {
        try {
            if (usernameExists(username)) {
                return false; // Username already exists
            }

            String hashedPassword = hashPassword(password);
            return saveUserToDatabase(username, hashedPassword, role);
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false; // Registration failed
        }
    }

    // Method to check if a username already exists in the database
    private boolean usernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Return true if count > 0
            }
        }
        return false; // Return false if no result or error occurs
    }

    // Method to save a new user to the database
    private boolean saveUserToDatabase(String username, String hashedPassword, String role) throws SQLException {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role); // Insert the user's role
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
            String hashedInputPassword = hashPassword(inputPassword);
            String query = "SELECT password, role FROM users WHERE username = ?";

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, inputUsername);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String storedRole = rs.getString("role");

                    if (storedPassword.equals(hashedInputPassword)) {
                        // Set the role of the user after successful login
                        this.role = storedRole;
                        return true; // Return true if login is successful
                    }
                }
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false; // Login failed
    }

    // Method to get the user's role
    public String getRole() {
        return role; // Return the role (e.g., "admin" or "user")
    }

    // Additional methods for other user-related operations can go here
}
