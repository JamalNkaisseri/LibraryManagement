package com.librarymanagement.gui;

import com.lms.Book;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
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
import org.apache.pdfbox.rendering.ImageType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * PDFViewer class for displaying PDF documents in the library management system.
 * This class follows the application's UI styling and integrates with the Book class.
 * Enhanced for better rendering quality with customizable DPI settings and responsive resizing.
 */
public class PDFViewer {
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private int totalPages = 0;
    private ImageView imageView;
    private Label pageLabel;
    private Book book;
    private float currentDpi = 150;  // Default DPI - increased from standard 72
    private ScrollPane scrollPane;
    private Label dpiLabel;
    private double scaleFactor = 1.0;
    private Stage pdfStage;

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
            // Don't set a fixed width here to allow resizing

            // Create page label
            pageLabel = new Label("Page 1 of " + totalPages);
            pageLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            // Create DPI label
            dpiLabel = new Label(String.format("Quality: %.0f DPI", currentDpi));
            dpiLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

            // Create navigation buttons
            Button prevButton = new Button("Previous");
            prevButton.getStyleClass().add("navigation-button");

            Button nextButton = new Button("Next");
            nextButton.getStyleClass().add("navigation-button");

            Button zoomInButton = new Button("Zoom In");
            zoomInButton.getStyleClass().add("zoom-button");

            Button zoomOutButton = new Button("Zoom Out");
            zoomOutButton.getStyleClass().add("zoom-button");

            Button fitWidthButton = new Button("Fit Width");
            fitWidthButton.getStyleClass().add("zoom-button");

            // Create quality slider
            Slider qualitySlider = new Slider(72, 300, currentDpi);
            qualitySlider.setShowTickMarks(true);
            qualitySlider.setShowTickLabels(true);
            qualitySlider.setMajorTickUnit(75);
            qualitySlider.setBlockIncrement(25);
            qualitySlider.setPrefWidth(200);

            // Add listeners to controls
            prevButton.setOnAction(e -> showPage(currentPage - 1));
            nextButton.setOnAction(e -> showPage(currentPage + 1));
            zoomInButton.setOnAction(e -> zoomIn());
            zoomOutButton.setOnAction(e -> zoomOut());
            fitWidthButton.setOnAction(e -> fitToWidth());
            qualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentDpi = newVal.floatValue();
                dpiLabel.setText(String.format("Quality: %.0f DPI", currentDpi));
                showPage(currentPage); // Refresh with new DPI
            });

            // Create top bar with book info
            Label bookInfoLabel = new Label(book.getTitle() + " by " + book.getAuthor());
            bookInfoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox topBar = new HBox(10, bookInfoLabel, spacer, pageLabel);
            topBar.setAlignment(Pos.CENTER_LEFT);
            topBar.setPadding(new Insets(10));
            topBar.setStyle("-fx-background-color: #f4f6f9; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

            // Create navigation controls
            HBox navigationControls = new HBox(10, prevButton, nextButton);
            navigationControls.setAlignment(Pos.CENTER);

            // Create zoom controls
            HBox zoomControls = new HBox(10, zoomOutButton, zoomInButton, fitWidthButton);
            zoomControls.setAlignment(Pos.CENTER);

            // Create quality controls
            HBox qualityControls = new HBox(10, dpiLabel, qualitySlider);
            qualityControls.setAlignment(Pos.CENTER);

            // Bottom controls container
            HBox controls = new HBox(30, navigationControls, zoomControls, qualityControls);
            controls.setAlignment(Pos.CENTER);
            controls.setPadding(new Insets(10));
            controls.setStyle("-fx-background-color: #f4f6f9; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");

            // Create ScrollPane for the PDF content
            scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: white;");
            scrollPane.setPannable(true);  // Enable panning with mouse

            // Set up the main layout
            BorderPane layout = new BorderPane();
            layout.setTop(topBar);
            layout.setCenter(scrollPane);
            layout.setBottom(controls);
            layout.setStyle("-fx-background-color: white;");

            // Create and show the stage
            pdfStage = new Stage();
            pdfStage.setTitle("PDF Viewer - " + book.getTitle());
            pdfStage.setScene(new Scene(layout, 900, 800));
            pdfStage.initOwner(parentStage);

            // Make sure the stage is resizable
            pdfStage.setResizable(true);

            // Apply the application's stylesheet if available
            try {
                pdfStage.getScene().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            } catch (Exception e) {
                // Stylesheet not found, continue without it
                System.out.println("Stylesheet not found, continuing without it");
            }

            // Add listener for window size changes
            pdfStage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                // Update view when window width changes
                if (imageView.getImage() != null) {
                    adaptImageToWindowSize(newWidth.doubleValue());
                }
            });

            // Show the first page
            showPage(0);

            pdfStage.show();

            // Initial fit to width
            fitToWidth();

            // Clean up when the window is closed
            pdfStage.setOnCloseRequest(e -> {
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
     * Display the specified page in the viewer with current DPI setting.
     *
     * @param pageNumber The page number to display (0-based)
     */
    private void showPage(int pageNumber) {
        if (pageNumber < 0 || pageNumber >= totalPages) {
            return;  // Invalid page number
        }

        try {
            // Save scroll position if applicable
            double vValue = 0;
            double hValue = 0;
            if (scrollPane != null) {
                vValue = scrollPane.getVvalue();
                hValue = scrollPane.getHvalue();
            }

            currentPage = pageNumber;

            // Use renderImageWithDPI for higher quality - specify RGB for better color quality
            BufferedImage image = null;

            // Try the newer method with ImageType parameter first
            try {
                image = renderer.renderImageWithDPI(currentPage, currentDpi, ImageType.RGB);
            } catch (NoSuchMethodError e) {
                // Fall back to the older method without ImageType if necessary
                try {
                    image = renderer.renderImageWithDPI(currentPage, currentDpi);
                } catch (NoSuchMethodError e2) {
                    // Ultimate fallback to the most basic method if needed
                    image = renderer.renderImage(currentPage);
                }
            }

            Image fxImage = SwingFXUtils.toFXImage(image, null);
            imageView.setImage(fxImage);

            // Apply current scale factor
            adaptImageToWindowSize(pdfStage.getWidth());

            pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);

            // Restore scroll position if it was a page change within the same zoom level
            if (scrollPane != null && (pageNumber == currentPage + 1 || pageNumber == currentPage - 1)) {
                scrollPane.setVvalue(vValue);
                scrollPane.setHvalue(hValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error Rendering Page", "Could not render page " + (pageNumber + 1) + ":\n" + e.getMessage());
        }
    }

    /**
     * Adjusts the image view to fit the current window size while accounting for scale factor.
     *
     * @param windowWidth The current width of the window
     */
    private void adaptImageToWindowSize(double windowWidth) {
        if (imageView.getImage() == null) return;

        // Account for ScrollPane borders and padding
        double availableWidth = windowWidth - 40; // Allow for scrollbar and padding

        // Apply the scale factor to the available width
        double targetWidth = availableWidth * scaleFactor;

        // Set the new fit width
        imageView.setFitWidth(targetWidth);
    }

    /**
     * Fits the PDF page to the width of the window.
     */
    private void fitToWidth() {
        scaleFactor = 1.0;
        adaptImageToWindowSize(pdfStage.getWidth());

        // Update DPI label to indicate fit to width mode
        dpiLabel.setText(String.format("Fit to Width (%.0f DPI)", currentDpi));
    }

    /**
     * Zoom in by increasing scale factor.
     */
    private void zoomIn() {
        // Save scroll position
        double vValue = scrollPane.getVvalue();
        double hValue = scrollPane.getHvalue();

        // Increase scale factor by 10%
        scaleFactor *= 1.1;

        // Apply the new scale
        adaptImageToWindowSize(pdfStage.getWidth());

        dpiLabel.setText(String.format("Zoom: %.0f%% (%.0f DPI)", scaleFactor * 100, currentDpi));

        // Restore scroll position
        scrollPane.setVvalue(vValue);
        scrollPane.setHvalue(hValue);
    }

    /**
     * Zoom out by decreasing scale factor.
     */
    private void zoomOut() {
        if (scaleFactor > 0.1) {  // Don't zoom out too much
            // Save scroll position
            double vValue = scrollPane.getVvalue();
            double hValue = scrollPane.getHvalue();

            // Decrease scale factor by 10%
            scaleFactor /= 1.1;

            // Apply the new scale
            adaptImageToWindowSize(pdfStage.getWidth());

            dpiLabel.setText(String.format("Zoom: %.0f%% (%.0f DPI)", scaleFactor * 100, currentDpi));

            // Restore scroll position
            scrollPane.setVvalue(vValue);
            scrollPane.setHvalue(hValue);
        }
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