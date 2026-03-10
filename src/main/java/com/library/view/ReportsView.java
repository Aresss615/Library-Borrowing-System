package com.library.view;

import com.library.model.BorrowRecord;
import com.library.service.BorrowService;
import com.library.utils.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

/**
 * Reports view with tabbed report tables and CSV export.
 */
public class ReportsView extends VBox {

    private final BorrowService borrowService = new BorrowService();
    private TabPane tabPane;

    public ReportsView() {
        setSpacing(20);
        setPadding(new Insets(8));
        build();
    }

    private void build() {
        Label title = new Label("Reports");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("View detailed borrowing reports and export data");
        subtitle.getStyleClass().add("page-subtitle");

        // ── Summary cards ──
        int borrowed = borrowService.getBorrowedRecords().size();
        int returned = borrowService.getReturnedRecords().size();
        int overdue  = borrowService.getOverdueRecords().size();

        HBox summary = new HBox(16);
        summary.setAlignment(Pos.CENTER_LEFT);
        summary.getChildren().addAll(
                createMiniCard("Currently Borrowed", String.valueOf(borrowed), "#007AFF"),
                createMiniCard("Total Returned",     String.valueOf(returned), "#34C759"),
                createMiniCard("Overdue",            String.valueOf(overdue),  "#FF3B30")
        );

        // ── Export button ──
        Button exportBtn = new Button("Export to CSV");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> handleExport());

        HBox toolbar = new HBox(exportBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        toolbar.setPadding(new Insets(0, 0, 4, 0));

        // ── Tab pane ──
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab borrowedTab = new Tab("Borrowed");
        borrowedTab.setContent(buildReportTable(borrowService.getBorrowedRecords()));

        Tab returnedTab = new Tab("Returned");
        returnedTab.setContent(buildReportTable(borrowService.getReturnedRecords()));

        Tab overdueTab = new Tab("Overdue");
        overdueTab.setContent(buildReportTable(borrowService.getOverdueRecords()));

        Tab allTab = new Tab("All Records");
        allTab.setContent(buildReportTable(borrowService.getAllRecords()));

        tabPane.getTabs().addAll(borrowedTab, returnedTab, overdueTab, allTab);

        getChildren().addAll(title, subtitle, summary, toolbar, tabPane);
    }

    // ── Report table builder ──

    private VBox buildReportTable(List<BorrowRecord> records) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));

        Label countLabel = new Label(records.size() + " record(s)");
        countLabel.getStyleClass().add("muted-text");

        TableView<BorrowRecord> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(records));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("No records to display"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colId = new TableColumn<>("#");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setMaxWidth(50);

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
                c.getValue().getReturnDate() != null ? c.getValue().getReturnDate().toString() : "\u2014"));

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
        colFine.setCellValueFactory(c -> new SimpleStringProperty("$" + c.getValue().calculateFine().toPlainString()));

        table.getColumns().addAll(colId, colUser, colBook, colBorrow, colDue, colReturn, colStatus, colFine);

        container.getChildren().addAll(countLabel, table);
        return container;
    }

    // ── CSV Export ──

    @SuppressWarnings("unchecked")
    private void handleExport() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;

        VBox content = (VBox) selectedTab.getContent();
        TableView<BorrowRecord> table = null;
        for (var node : content.getChildren()) {
            if (node instanceof TableView<?>) {
                table = (TableView<BorrowRecord>) node;
                break;
            }
        }

        if (table == null || table.getItems().isEmpty()) {
            AlertHelper.showWarning("No Data", "No records to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to CSV");
        fileChooser.setInitialFileName("library_report_" +
                selectedTab.getText().toLowerCase().replace(" ", "_") + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("ID,User,Book,Borrow Date,Due Date,Return Date,Status,Fine");
                for (BorrowRecord record : table.getItems()) {
                    pw.println(String.join(",",
                            String.valueOf(record.getId()),
                            escapeCsv(record.getUserName()),
                            escapeCsv(record.getBookTitle()),
                            record.getBorrowDate().toString(),
                            record.getDueDate().toString(),
                            record.getReturnDate() != null ? record.getReturnDate().toString() : "",
                            record.getStatusDisplay(),
                            "$" + record.calculateFine().toPlainString()
                    ));
                }
                AlertHelper.showSuccess("Exported", "Records exported to " + file.getName());
            } catch (Exception e) {
                AlertHelper.showError("Export Failed", "Failed to export: " + e.getMessage());
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ── Mini card builder ──

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
