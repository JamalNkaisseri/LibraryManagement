package com.lms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class User {
    private static final DatabaseConnection dbConnection = new DatabaseConnection();
    private final String username;
    private final String password;
    private String role;
    private int userId; // Added to store the user's ID

    // Constructor for new user registration
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Constructor for an existing user (with role)
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = fetchUserId(); // Get user ID when creating existing user
    }

    // Method to fetch user ID from database
    private int fetchUserId() {
        try {
            String query = "SELECT id FROM users WHERE username = ?";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Method to register a new user
    public boolean register(String role) {
        try {
            if (usernameExists(username)) {
                return false;
            }

            String hashedPassword = hashPassword(password);
            return saveUserToDatabase(username, hashedPassword, role);
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
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
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Method to save a new user to the database
    private boolean saveUserToDatabase(String username, String hashedPassword, String role) throws SQLException {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            return true;
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

    public boolean login(String inputUsername, String inputPassword) {
        try {
            String hashedInputPassword = hashPassword(inputPassword);
            String query = "SELECT id, password, role FROM users WHERE username = ?";

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, inputUsername);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String storedRole = rs.getString("role");
                    int storedId = rs.getInt("id");

                    if (storedPassword.equals(hashedInputPassword)) {
                        this.role = storedRole;
                        this.userId = storedId;
                        return true;
                    }
                }
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(String currentPassword, String newPassword) {
        try {
            // Verify current password
            String hashedCurrentPassword = hashPassword(currentPassword);
            String query = "SELECT password FROM users WHERE username = ?";

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next() && rs.getString("password").equals(hashedCurrentPassword)) {
                    // Update password
                    String hashedNewPassword = hashPassword(newPassword);
                    String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, hashedNewPassword);
                        updateStmt.setString(2, username);
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static class UserStats {
        private final int totalBorrowed;
        private final int currentlyBorrowed;
        private final double totalFines;

        public UserStats(int totalBorrowed, int currentlyBorrowed, double totalFines) {
            this.totalBorrowed = totalBorrowed;
            this.currentlyBorrowed = currentlyBorrowed;
            this.totalFines = totalFines;
        }

        public int getTotalBorrowed() { return totalBorrowed; }
        public int getCurrentlyBorrowed() { return currentlyBorrowed; }
        public double getTotalFines() { return totalFines; }
    }

    public UserStats getUserStats() {
        try {
            String query = """
                SELECT 
                    COUNT(*) as total_borrowed,
                    SUM(CASE WHEN return_date IS NULL AND status = 'Borrowed' THEN 1 ELSE 0 END) as currently_borrowed,
                    SUM(fine) as total_fines
                FROM loan
                WHERE user_id = ?
            """;

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return new UserStats(
                            rs.getInt("total_borrowed"),
                            rs.getInt("currently_borrowed"),
                            rs.getDouble("total_fines")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new UserStats(0, 0, 0.0);
    }

    // Getter methods
    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    // Constructor for full user details
    public User(String username, String email, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = fetchUserId(); // Get user ID when creating existing user
    }

    // Method to register a new user from admin dashboard
    public boolean registerUser() {
        return register(role);
    }

    // Method to delete a user
    public boolean deleteUser() {
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

    // Method to view all users
    public static List<User> viewAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            String query = "SELECT username, role FROM users";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String username = rs.getString("username");
                    String role = rs.getString("role");
                    // Using a dummy password as we don't want to retrieve actual passwords
                    users.add(new User(username, "dummy", role));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}