package com.lms;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SearchService search = new SearchService();

        addBook();
    }


    // Method to handle the process of adding a book
    private static void addBook() {
        // Set up Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // Prompt for book details
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();

        System.out.print("Enter book author: ");
        String author = scanner.nextLine();

        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        System.out.print("Enter book category: ");
        String category = scanner.nextLine();

        System.out.print("Enter the number of copies: ");
        int totalCopies = scanner.nextInt(); // Ask for the number of copies

        // Get the category ID based on the category name
        DatabaseConnection dbConnection = new DatabaseConnection();
        int categoryId = dbConnection.getCategoryId(category);

        if (categoryId == -1) {
            System.out.println("Invalid category. Please try again.");
        } else {
            // Create a new book instance with the given details and totalCopies
            Book newBook = new Book(title, author, isbn, categoryId, totalCopies);

            // Attempt to add the book to the database and print the result
            if (newBook.addBookToDatabase()) {
                System.out.println("Book added successfully.");
            } else {
                System.out.println("Failed to add the book.");
            }
        }

        scanner.close(); // Close the scanner after input is complete
    }


    // Method to view and display all books
    private static void viewBooks() {
        List<Book> books = Book.viewAllBooks(); // Call the static method to get all books

        if (books.isEmpty()) {
            System.out.println("No books available in the database.");
        } else {
            System.out.println("Books in the database:");
            for (Book book : books) {
                System.out.println("ID: " + book.getId() + ", Title: " + book.getTitle() +
                        ", Author: " + book.getAuthor() + ", ISBN: " + book.getIsbn() +
                        ", Category ID: " + book.getCategoryId());
            }
        }
    }

    // Method to search for books by title or author
    private static void searchBooks() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Do you want to search by title or author? ");
        String searchType = scanner.nextLine().trim().toLowerCase();

        SearchService searchService = new SearchService();
        List<Book> books;

        if (searchType.equals("title")) {
            System.out.print("Enter the title to search: ");
            String title = scanner.nextLine();
            books = searchService.searchByTitle(title);
        } else if (searchType.equals("author")) {
            System.out.print("Enter the author to search: ");
            String author = scanner.nextLine();
            books = searchService.searchByAuthor(author);
        } else {
            System.out.println("Invalid search type. Please enter 'title' or 'author'.");
            return;
        }

        // Display the search results
        if (books.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println("Search Results:");
            for (Book book : books) {
                System.out.println("ID: " + book.getId() + ", Title: " + book.getTitle() +
                        ", Author: " + book.getAuthor() + ", ISBN: " + book.getIsbn() +
                        ", Category ID: " + book.getCategoryId());
            }
        }
    }

}
