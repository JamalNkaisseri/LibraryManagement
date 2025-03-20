package com.librarymanagement.gui;

import com.lms.Book;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * PDFViewer class for displaying PDF documents in the library management system.
 * This class follows the application's UI styling and integrates with the Book class.
 */
public class PDFViewer {
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private int totalPages = 0;
    private ImageView imageView;
    private Label pageLabel;
    private Book book;

    /**
     * Opens a PDF file and displays it in a new window.
     *
     * @param book The book whose PDF should be opened
     * @param pdfFile The PDF file to open
     * @param parentStage The parent stage for modal behavior
     */
    public void openPDF(Book book, File pdfFile, Stage parentStage) {
        this.book = book;

        try {
            // Load the PDF document
            document = PDDocument.load(pdfFile);
            renderer = new PDFRenderer(document);
            totalPages = document.getNumberOfPages();

            // Create the UI components
            imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(800);

            // Create page label
            pageLabel = new Label("Page 1 of " + totalPages);
            pageLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            // Create navigation buttons
            Button prevButton = new Button("Previous");
            prevButton.getStyleClass().add("navigation-button");

            Button nextButton = new Button("Next");
            nextButton.getStyleClass().add("navigation-button");

            Button zoomInButton = new Button("Zoom In");
            zoomInButton.getStyleClass().add("zoom-button");

            Button zoomOutButton = new Button("Zoom Out");
            zoomOutButton.getStyleClass().add("zoom-button");

            // Set up button actions
            prevButton.setOnAction(e -> showPage(currentPage - 1));
            nextButton.setOnAction(e -> showPage(currentPage + 1));
            zoomInButton.setOnAction(e -> zoomIn());
            zoomOutButton.setOnAction(e -> zoomOut());

            // Create top bar with book info
            Label bookInfoLabel = new Label(book.getTitle() + " by " + book.getAuthor());
            bookInfoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox topBar = new HBox(10, bookInfoLabel, spacer, pageLabel);
            topBar.setAlignment(Pos.CENTER_LEFT);
            topBar.setPadding(new Insets(10));
            topBar.setStyle("-fx-background-color: #f4f6f9; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

            // Create button container
            HBox controls = new HBox(10, prevButton, nextButton, zoomInButton, zoomOutButton);
            controls.setAlignment(Pos.CENTER);
            controls.setPadding(new Insets(10));
            controls.setStyle("-fx-background-color: #f4f6f9; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");

            // Create ScrollPane for the PDF content
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: white;");

            // Set up the main layout
            BorderPane layout = new BorderPane();
            layout.setTop(topBar);
            layout.setCenter(scrollPane);
            layout.setBottom(controls);
            layout.setStyle("-fx-background-color: white;");

            // Show the first page
            showPage(0);

            // Create and show the stage
            Stage stage = new Stage();
            stage.setTitle("PDF Viewer - " + book.getTitle());
            stage.setScene(new Scene(layout, 900, 800));
            stage.initOwner(parentStage);

            // Apply the application's stylesheet if available
            try {
                stage.getScene().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            } catch (Exception e) {
                // Stylesheet not found, continue without it
                System.out.println("Stylesheet not found, continuing without it");
            }

            stage.show();

            // Clean up when the window is closed
            stage.setOnCloseRequest(e -> {
                try {
                    document.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error Opening PDF", "Could not open the PDF file:\n" + e.getMessage());
        }
    }

    /**
     * Display the specified page in the viewer.
     *
     * @param pageNumber The page number to display (0-based)
     */
    private void showPage(int pageNumber) {
        if (pageNumber < 0 || pageNumber >= totalPages) {
            return;  // Invalid page number
        }

        try {
            currentPage = pageNumber;
            BufferedImage image = renderer.renderImage(currentPage);
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            imageView.setImage(fxImage);
            pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error Rendering Page", "Could not render page " + (pageNumber + 1) + ":\n" + e.getMessage());
        }
    }

    /**
     * Zoom in on the current page.
     */
    private void zoomIn() {
        imageView.setFitWidth(imageView.getFitWidth() * 1.1);
    }

    /**
     * Zoom out on the current page.
     */
    private void zoomOut() {
        imageView.setFitWidth(imageView.getFitWidth() / 1.1);
    }

    /**
     * Display an error alert dialog.
     */
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}