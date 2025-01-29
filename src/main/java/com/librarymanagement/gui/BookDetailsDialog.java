
package com.librarymanagement.gui;

import com.lms.Book;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class BookDetailsDialog {
    private final Book book;
    private final Stage dialog;

    public BookDetailsDialog(Book book, Stage parentStage) {
        this.book = book;
        this.dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.setTitle("Book Details");
        dialog.setMinWidth(400);
        dialog.setMinHeight(300);

        createContent();
    }

    private void createContent() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f4f6f9;");

        // Title section
        Label titleHeader = new Label(book.getTitle());
        titleHeader.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Author section
        Label authorHeader = new Label("By " + book.getAuthor());
        authorHeader.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");

        // Details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(20, 0, 20, 0));

        // Add details to grid
        addDetailRow(detailsGrid, 0, "ISBN:", book.getIsbn());
        addDetailRow(detailsGrid, 1, "Category:", getCategoryName(book.getCategoryId()));
        addDetailRow(detailsGrid, 2, "Available Copies:", String.valueOf(book.getAvailableCopies()));
        addDetailRow(detailsGrid, 3, "Total Copies:", String.valueOf(book.getTotalCopies()));

        // Status section
        String statusText = book.getAvailableCopies() > 0 ? "Available" : "Not Available";
        String statusColor = book.getAvailableCopies() > 0 ? "#2ecc71" : "#e74c3c";
        Label statusLabel = new Label("Status: " + statusText);
        statusLabel.setStyle(String.format("-fx-font-size: 14px; -fx-text-fill: %s; -fx-font-weight: bold;", statusColor));

        // Close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("dialog-button");
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add all components to main container
        mainContainer.getChildren().addAll(
                titleHeader,
                authorHeader,
                detailsGrid,
                statusLabel,
                buttonBox
        );

        Scene scene = new Scene(mainContainer);
        // Add the same stylesheet used in the main application
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        dialog.setScene(scene);
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        Label valueNode = new Label(value);

        labelNode.setStyle("-fx-font-weight: bold;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private String getCategoryName(int categoryId) {
        return switch (categoryId) {
            case 1 -> "Fiction";
            case 2 -> "Non-Fiction";
            case 3 -> "Science Fiction";
            case 4 -> "Biography";
            default -> "Unknown";
        };
    }

    public void show() {
        dialog.showAndWait();
    }
}