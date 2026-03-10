package com.library.view;

import com.library.model.BorrowRecord;
import com.library.service.BorrowService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Reports view: tabbed interface showing borrowed, returned, and overdue report tables.
 */
public class ReportsView extends VBox {

    private final BorrowService borrowService = new BorrowService();

    public ReportsView() {
        setSpacing(20);
        setPadding(new Insets(8));
        build();
    }

    private void build() {
        Label title = new Label("Reports");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("View detailed borrowing reports");
        subtitle.getStyleClass().add("page-subtitle");

        // Tab pane for different report types
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Borrowed report tab
        Tab borrowedTab = new Tab("📖 Borrowed Books");
        borrowedTab.setContent(buildReportTable(borrowService.getBorrowedRecords(), "borrowed"));

        // Returned report tab
        Tab returnedTab = new Tab("✅ Returned Books");
        returnedTab.setContent(buildReportTable(borrowService.getReturnedRecords(), "returned"));

        // Overdue report tab
        Tab overdueTab = new Tab("⚠️ Overdue Books");
        overdueTab.setContent(buildReportTable(borrowService.getOverdueRecords(), "overdue"));

        // All records tab
        Tab allTab = new Tab("📋 All Records");
        allTab.setContent(buildReportTable(borrowService.getAllRecords(), "all"));

        tabPane.getTabs().addAll(borrowedTab, returnedTab, overdueTab, allTab);

        // Summary cards
        HBox summary = new HBox(16);
        summary.setAlignment(Pos.CENTER_LEFT);

        int borrowed = borrowService.getBorrowedRecords().size();
        int returned = borrowService.getReturnedRecords().size();
        int overdue  = borrowService.getOverdueRecords().size();

        summary.getChildren().addAll(
            createMiniCard("Currently Borrowed", String.valueOf(borrowed), "#007AFF"),
            createMiniCard("Total Returned",     String.valueOf(returned), "#34C759"),
            createMiniCard("Overdue",            String.valueOf(overdue),  "#FF3B30")
        );

        getChildren().addAll(title, subtitle, summary, tabPane);
    }

    /** Build a report table for a given list of records */
    private VBox buildReportTable(List<BorrowRecord> records, String type) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));

        TableView<BorrowRecord> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(records));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("No records to display"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colId = new TableColumn<>("#");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setMinWidth(40);

        TableColumn<BorrowRecord, String> colUser = new TableColumn<>("User");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUserName()));
        colUser.setMinWidth(130);

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Book");
        colBook.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookTitle()));
        colBook.setMinWidth(180);

        TableColumn<BorrowRecord, String> colBorrow = new TableColumn<>("Borrow Date");
        colBorrow.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBorrowDate().toString()));

        TableColumn<BorrowRecord, String> colDue = new TableColumn<>("Due Date");
        colDue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDueDate().toString()));

        TableColumn<BorrowRecord, String> colReturn = new TableColumn<>("Return Date");
        colReturn.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getReturnDate() != null ? c.getValue().getReturnDate().toString() : "—"));

        TableColumn<BorrowRecord, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusDisplay()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    switch (item) {
                        case "Borrowed" -> badge.getStyleClass().add("badge-borrowed");
                        case "Returned" -> badge.getStyleClass().add("badge-returned");
                        case "Overdue"  -> badge.getStyleClass().add("badge-overdue");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<BorrowRecord, String> colFine = new TableColumn<>("Fine");
        colFine.setCellValueFactory(c -> new SimpleStringProperty("$" + c.getValue().calculateFine().toString()));

        table.getColumns().addAll(colId, colUser, colBook, colBorrow, colDue, colReturn, colStatus, colFine);

        // Record count
        Label countLabel = new Label(records.size() + " record(s)");
        countLabel.getStyleClass().add("muted-text");

        container.getChildren().addAll(countLabel, table);
        return container;
    }

    /** Create a mini summary card */
    private VBox createMiniCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("glass-panel");
        card.setMinWidth(180);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 0 0 3; -fx-border-radius: 16;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-card-label");

        card.getChildren().addAll(valueLabel, textLabel);
        return card;
    }
}
