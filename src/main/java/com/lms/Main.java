package com.lms;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Prompt the user for their username
        System.out.println("Enter username:");
        String username = input.nextLine();

        // Prompt the user for their password
        System.out.println("Enter password:");
        String password = input.nextLine();

        // Create a User object without username and password as arguments
        User user = new User(username, password);

        // Attempt to log in using the provided username and password
        if (user.login(username, password)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }

        // Close the scanner to prevent resource leaks
        input.close();
    }
}
