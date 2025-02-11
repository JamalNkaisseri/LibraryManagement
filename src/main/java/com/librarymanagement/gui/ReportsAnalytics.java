package com.librarymanagement.gui;

import com.lms.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ReportsAnalytics {
    private final DatabaseConnection dbConnection = new DatabaseConnection();

    public VBox createReportsSection() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label("Library Reports & Analytics");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Create TabPane for different report categories
        TabPane reportTabs = new TabPane();
        reportTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Add different report tabs
        reportTabs.getTabs().addAll(
                createOverviewTab(),
                createBorrowingTrendsTab(),
                createCategoryAnalysisTab()
        );

        mainContainer.getChildren().addAll(headerLabel, reportTabs);
        return mainContainer;
    }

    private LineChart<String, Number> createBorrowingTrendChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Daily Borrowing Trends");
        xAxis.setLabel("Date");
        yAxis.setLabel("Number of Borrows");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Borrows");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DATE(borrow_date) as borrow_day, COUNT(*) as borrow_count " +
                             "FROM borrowed_books " +
                             "WHERE borrow_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
                             "GROUP BY DATE(borrow_date) " +
                             "ORDER BY borrow_day")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getString("borrow_day"),
                        rs.getInt("borrow_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    private Tab createOverviewTab() {
        Tab overviewTab = new Tab("Overview");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));

        // Add statistics cards
        grid.add(createStatCard("Total Books", getTotalBooks()), 0, 0);
        grid.add(createStatCard("Books Currently Borrowed", getBorrowedBooks()), 1, 0);
        grid.add(createStatCard("Total Users", getTotalUsers()), 2, 0);
        grid.add(createStatCard("Active Borrowers", getActiveBorrowers()), 3, 0);

        // Add monthly statistics chart
        BarChart<String, Number> monthlyStats = createMonthlyStatisticsChart();
        grid.add(monthlyStats, 0, 1, 4, 1);

        overviewTab.setContent(grid);
        return overviewTab;
    }

    private Tab createBorrowingTrendsTab() {
        Tab trendsTab = new Tab("Borrowing Trends");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Add line chart for borrowing trends over time
        LineChart<String, Number> trendChart = createBorrowingTrendChart();

        // Add most borrowed books chart
        BarChart<String, Number> popularBooks = createPopularBooksChart();

        content.getChildren().addAll(trendChart, popularBooks);
        trendsTab.setContent(content);
        return trendsTab;
    }

    private Tab createCategoryAnalysisTab() {
        Tab categoryTab = new Tab("Category Analysis");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Add pie chart for book distribution by category
        PieChart categoryDistribution = createCategoryDistributionChart();

        // Add bar chart for category borrowing frequency
        BarChart<String, Number> categoryBorrowingChart = createCategoryBorrowingChart();

        content.getChildren().addAll(categoryDistribution, categoryBorrowingChart);
        categoryTab.setContent(content);
        return categoryTab;
    }

    private VBox createStatCard(String title, int value) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private BarChart<String, Number> createMonthlyStatisticsChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle("Monthly Statistics");
        xAxis.setLabel("Month");
        yAxis.setLabel("Count");

        XYChart.Series<String, Number> borrowsSeries = new XYChart.Series<>();
        borrowsSeries.setName("Borrows");

        // Using your database schema
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT MONTH(borrow_date) as month, COUNT(*) as count " +
                             "FROM borrowed_books " +
                             "WHERE YEAR(borrow_date) = YEAR(CURRENT_DATE) " +
                             "GROUP BY MONTH(borrow_date) " +
                             "ORDER BY month")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String month = getMonthName(rs.getInt("month"));
                borrowsSeries.getData().add(new XYChart.Data<>(month, rs.getInt("count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        barChart.getData().add(borrowsSeries);
        return barChart;
    }

    private BarChart<String, Number> createPopularBooksChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle("Most Popular Books");
        xAxis.setLabel("Book Title");
        yAxis.setLabel("Times Borrowed");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Borrow Count");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.title, COUNT(*) as borrow_count " +
                             "FROM borrowed_books bb " +
                             "JOIN books b ON bb.book_id = b.book_id " +
                             "GROUP BY b.book_id, b.title " +
                             "ORDER BY borrow_count DESC " +
                             "LIMIT 5")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getString("title"),
                        rs.getInt("borrow_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        barChart.getData().add(series);
        return barChart;
    }

    private BarChart<String, Number> createCategoryBorrowingChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle("Borrowing by Category");
        xAxis.setLabel("Category");
        yAxis.setLabel("Number of Borrows");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Borrow Count");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.name, COUNT(*) as borrow_count " +
                             "FROM borrowed_books bb " +
                             "JOIN books b ON bb.book_id = b.book_id " +
                             "JOIN category c ON b.category_id = c.category_id " +
                             "GROUP BY c.category_id, c.name " +
                             "ORDER BY borrow_count DESC")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getString("name"),
                        rs.getInt("borrow_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        barChart.getData().add(series);
        return barChart;
    }

    private PieChart createCategoryDistributionChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Books Distribution by Category");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.name, COUNT(*) as book_count " +
                             "FROM books b " +
                             "JOIN category c ON b.category_id = c.category_id " +
                             "GROUP BY c.category_id, c.name")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pieChart.getData().add(new PieChart.Data(
                        rs.getString("name"),
                        rs.getInt("book_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pieChart;
    }

    private int getTotalBooks() {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM books")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getBorrowedBooks() {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM borrowed_books WHERE return_date IS NULL")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalUsers() {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getActiveBorrowers() {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(DISTINCT user_id) FROM borrowed_books WHERE return_date IS NULL")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getMonthName(int month) {
        return new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"}[month - 1];
    }
}