package com.lms;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class User {


    private String userName;
    private String password;

    public User(String userName, String password){
        this.userName = userName;
        this.password = password;
    }

    public boolean register(){

        try{
            if(userNameExists(userName)){
                return false; //Username already exists
            }

            String hashedPassword = hashPassword(password);
            return  saveUserToDatabase(userName,hashedPassword);
        }catch (SQLException | NoSuchAlgorithmException e){
            e.printStackTrace();
            return false;//Registration failed
        }
    }

    private boolean usernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Return true if count is greater than 0
            }
        }
        return false; // Return false if any error occurs or if no results
    }
}
