package com.lms;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Call the viewAllBooks method to retrieve the list of books
        List<Book> books = Book.viewAllBooks();

        // Display the books
        if (books.isEmpty()) {
            System.out.println("No books found in the database.");
        } else {
            System.out.println("Books in the database:");
            for (Book book : books) {
                System.out.println("ID: " + book.getId() +
                        ", Title: " + book.getTitle() +
                        ", Author: " + book.getAuthor() +
                        ", ISBN: " + book.getIsbn() +
                        ", Category ID: " + book.getCategoryId());
            }
        }
    }
}
