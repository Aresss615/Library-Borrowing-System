package com.library.view;

import com.library.database.BookDAO;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.DashboardStats;
import com.library.service.BorrowService;
import com.library.service.DashboardService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enhanced Dashboard with:
 *   • Statistics cards (6 metrics)
 *   • Recent Activity feed (last 8 transactions)
 *   • Low Stock Alerts panel
 *   • Welcome message
 */
public class DashboardView extends VBox {

    private final DashboardService dashboardService = new DashboardService();
    private final BorrowService borrowService = new BorrowService();
    private final BookDAO bookDAO = new BookDAO();

    public DashboardView() {
        setSpacing(24);
        setPadding(new Insets(8));
        build();
    }

    private void build() {
        // ── Header ──
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Overview of your library system");
        subtitle.getStyleClass().add("page-subtitle");

        // ── Stats cards ──
        DashboardStats stats = dashboardService.getStats();

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(33.33);
            statsGrid.getColumnConstraints().add(cc);
        }

        statsGrid.add(createStatCard("Total Books",      String.valueOf(stats.getTotalBooks()),     "stat-card-blue"),   0, 0);
        statsGrid.add(createStatCard("Borrowed",          String.valueOf(stats.getTotalBorrowed()),  "stat-card-orange"), 1, 0);
        statsGrid.add(createStatCard("Students",          String.valueOf(stats.getTotalUsers()),     "stat-card-green"),  2, 0);
        statsGrid.add(createStatCard("Overdue",           String.valueOf(stats.getOverdueBooks()),   "stat-card-red"),    0, 1);
        statsGrid.add(createStatCard("Available Copies",  String.valueOf(stats.getTotalAvailable()), "stat-card-teal"),   1, 1);
        statsGrid.add(createStatCard("Returned",          String.valueOf(stats.getTotalReturned()),  "stat-card-purple"), 2, 1);

        // ── Bottom two-column section ──
        HBox bottomSection = new HBox(20);

        VBox activityPanel = buildActivityPanel();
        HBox.setHgrow(activityPanel, Priority.ALWAYS);

        VBox rightColumn = new VBox(20);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        rightColumn.getChildren().addAll(buildLowStockPanel(), buildWelcomePanel());

        bottomSection.getChildren().addAll(activityPanel, rightColumn);

        getChildren().addAll(title, subtitle, statsGrid, bottomSection);
    }

    // ── Stat card builder ──

    private VBox createStatCard(String label, String value, String colorClass) {
        VBox card = new VBox(4);
        card.getStyleClass().addAll("stat-card", colorClass);
        card.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-card-value");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-card-label");

        card.getChildren().addAll(valueLabel, textLabel);
        return card;
    }

    // ── Recent Activity panel ──

    private VBox buildActivityPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("glass-panel");

        Label panelTitle = new Label("Recent Activity");
        panelTitle.getStyleClass().add("panel-title");

        List<BorrowRecord> recent = borrowService.getRecentRecords(8);

        VBox list = new VBox(0);
        if (recent.isEmpty()) {
            Label empty = new Label("No recent activity");
            empty.getStyleClass().add("muted-text");
            list.getChildren().add(empty);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm");
            for (BorrowRecord record : recent) {
                HBox item = new HBox(12);
                item.getStyleClass().add("activity-item");
                item.setAlignment(Pos.CENTER_LEFT);

                String icon = record.getStatus() == BorrowRecord.Status.RETURNED ? "\u21A9" : "\uD83D\uDCD6";
                Label iconLabel = new Label(icon);
                iconLabel.setStyle("-fx-font-size: 16px; -fx-min-width: 24;");

                String description;
                if (record.getStatus() == BorrowRecord.Status.RETURNED) {
                    description = record.getUserName() + " returned \"" + record.getBookTitle() + "\"";
                } else if (record.isOverdue()) {
                    description = record.getUserName() + " \u2014 \"" + record.getBookTitle() + "\" is overdue";
                } else {
                    description = record.getUserName() + " borrowed \"" + record.getBookTitle() + "\"";
                }

                VBox textBox = new VBox(2);
                Label descLabel = new Label(description);
                descLabel.getStyleClass().add("activity-text");
                descLabel.setWrapText(true);

                Label timeLabel = new Label(record.getUpdatedAt() != null ? record.getUpdatedAt().format(fmt) : "");
                timeLabel.getStyleClass().add("activity-time");

                textBox.getChildren().addAll(descLabel, timeLabel);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                Label badge = new Label(record.getStatusDisplay());
                badge.getStyleClass().add("badge");
                switch (record.getStatusDisplay()) {
                    case "Borrowed" -> badge.getStyleClass().add("badge-borrowed");
                    case "Returned" -> badge.getStyleClass().add("badge-returned");
                    case "Overdue"  -> badge.getStyleClass().add("badge-overdue");
                }

                item.getChildren().addAll(iconLabel, textBox, badge);
                list.getChildren().add(item);
            }
        }

        panel.getChildren().addAll(panelTitle, list);
        return panel;
    }

    // ── Low Stock Alerts panel ──

    private VBox buildLowStockPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("glass-panel");
        panel.setStyle("-fx-border-color: #FF9500; -fx-border-width: 0 0 0 4; -fx-border-radius: 16;");

        Label panelTitle = new Label("Low Stock Alerts");
        panelTitle.getStyleClass().add("panel-title");

        List<Book> lowStock = bookDAO.findLowStock();

        if (lowStock.isEmpty()) {
            Label ok = new Label("All books are well stocked \u2714");
            ok.getStyleClass().add("muted-text");
            panel.getChildren().addAll(panelTitle, ok);
        } else {
            VBox rows = new VBox(8);
            for (Book book : lowStock) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                Label warn = new Label("\u26A0");
                warn.setStyle("-fx-font-size: 14px;");

                Label text = new Label(book.getTitle() + " \u2014 " +
                        book.getAvailableCopies() + "/" + book.getTotalCopies() + " available");
                text.getStyleClass().add("activity-text");
                text.setWrapText(true);
                HBox.setHgrow(text, Priority.ALWAYS);

                row.getChildren().addAll(warn, text);
                rows.getChildren().add(row);
            }
            panel.getChildren().addAll(panelTitle, rows);
        }
        return panel;
    }

    // ── Welcome panel ──

    private VBox buildWelcomePanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("glass-panel");

        Label panelTitle = new Label("Welcome to Library System");
        panelTitle.getStyleClass().add("panel-title");

        Label text = new Label(
            "Manage your library efficiently. Use the sidebar to navigate between " +
            "books, users, borrowing records, and reports. The system automatically " +
            "tracks overdue books and calculates fines."
        );
        text.setWrapText(true);
        text.getStyleClass().add("muted-text");
        text.setStyle("-fx-font-size: 13px;");

        panel.getChildren().addAll(panelTitle, text);
        return panel;
    }
}
