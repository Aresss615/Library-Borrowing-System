package com.library.view;

import com.library.model.DashboardStats;
import com.library.service.DashboardService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Dashboard view: displays statistics cards with counts for
 * total books, borrowed, users, overdue, available, and returned.
 */
public class DashboardView extends VBox {

    private final DashboardService dashboardService = new DashboardService();

    public DashboardView() {
        setSpacing(24);
        setPadding(new Insets(8));
        build();
    }

    private void build() {
        // Page header
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Overview of your library system");
        subtitle.getStyleClass().add("page-subtitle");

        // Fetch stats
        DashboardStats stats = dashboardService.getStats();

        // Build stat cards in a 3-column grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // Make columns grow evenly
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(33.33);
            grid.getColumnConstraints().add(cc);
        }

        grid.add(createCard("📚", "Total Books",     String.valueOf(stats.getTotalBooks()),     "stat-card-blue"),   0, 0);
        grid.add(createCard("📖", "Borrowed",         String.valueOf(stats.getTotalBorrowed()),  "stat-card-orange"), 1, 0);
        grid.add(createCard("👥", "Total Users",      String.valueOf(stats.getTotalUsers()),     "stat-card-green"),  2, 0);
        grid.add(createCard("⚠️", "Overdue",           String.valueOf(stats.getOverdueBooks()),   "stat-card-red"),    0, 1);
        grid.add(createCard("✅", "Available Copies",  String.valueOf(stats.getTotalAvailable()),  "stat-card-teal"),   1, 1);
        grid.add(createCard("🔄", "Returned",          String.valueOf(stats.getTotalReturned()),   "stat-card-purple"), 2, 1);

        // Welcome panel
        VBox welcomePanel = new VBox(12);
        welcomePanel.getStyleClass().add("glass-panel");
        Label welcomeTitle = new Label("Welcome to Library Borrowing System");
        welcomeTitle.getStyleClass().add("panel-title");
        Label welcomeText = new Label(
            "Manage your library efficiently. Use the sidebar to navigate between " +
            "books, users, borrowing records, and reports. The system automatically " +
            "tracks overdue books and calculates fines."
        );
        welcomeText.setWrapText(true);
        welcomeText.getStyleClass().add("muted-text");
        welcomeText.setStyle("-fx-font-size: 14px;");
        welcomePanel.getChildren().addAll(welcomeTitle, welcomeText);

        getChildren().addAll(title, subtitle, grid, welcomePanel);
    }

    /** Create a single stat card */
    private VBox createCard(String icon, String label, String value, String colorClass) {
        VBox card = new VBox(4);
        card.getStyleClass().addAll("stat-card", colorClass);
        card.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-card-icon");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-card-value");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-card-label");

        card.getChildren().addAll(iconLabel, valueLabel, textLabel);
        return card;
    }
}
