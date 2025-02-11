package com.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final DatabaseConnection dbConnection = new DatabaseConnection();

    // Create a new user
    public boolean createUser(String username, String password, String role) {
        try {
            // Check if username already exists
            if (userExists(username)) {
                return false;
            }

            // Hash the password
            String hashedPassword = hashPassword(password);

            // Default role to 'user' if not specified
            role = (role == null || role.isEmpty()) ? "user" : role;

            // Insert user into database
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, role);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if user exists
    public boolean userExists(String username) {
        try {
            String query = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete a user
    public boolean deleteUser(String username) {
        try {
            String query = "DELETE FROM users WHERE username = ?";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            String query = "SELECT username, role FROM users";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String username = rs.getString("username");
                    String role = rs.getString("role");
                    // Note: We use a dummy password for security reasons
                    users.add(new User(username, "dummy", role));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Update user role
    public boolean updateUserRole(String username, String newRole) {
        try {
            String query = "UPDATE users SET role = ? WHERE username = ?";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, newRole);
                pstmt.setString(2, username);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hash password (same as in User class)
    private String hashPassword(String password) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] byteData = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : byteData) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}