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
 * Overdue tracking view: shows all overdue books with fines.
 */
public class OverdueView extends VBox {

    private final BorrowService borrowService = new BorrowService();
    private TableView<BorrowRecord> table;

    public OverdueView() {
        setSpacing(20);
        setPadding(new Insets(8));
        build();
        loadOverdue();
    }

    private void build() {
        Label title = new Label("Overdue Books");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Books past their due date");
        subtitle.getStyleClass().add("page-subtitle");

        // Info card
        HBox infoCard = new HBox(16);
        infoCard.getStyleClass().addAll("glass-panel");
        infoCard.setAlignment(Pos.CENTER_LEFT);
        infoCard.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 0 0 0 4; -fx-border-radius: 16;");
        Label infoIcon = new Label("⚠️");
        infoIcon.setStyle("-fx-font-size: 24px;");
        VBox infoText = new VBox(4);
        Label infoTitle = new Label("Automatic Overdue Detection");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label infoDesc = new Label("Books are automatically marked as overdue when the due date has passed. A fine of $1.00 per day is applied.");
        infoDesc.getStyleClass().add("muted-text");
        infoDesc.setWrapText(true);
        infoText.getChildren().addAll(infoTitle, infoDesc);
        HBox.setHgrow(infoText, Priority.ALWAYS);
        infoCard.getChildren().addAll(infoIcon, infoText);

        // Table
        VBox tablePanel = new VBox();
        tablePanel.getStyleClass().add("glass-panel");
        VBox.setVgrow(tablePanel, Priority.ALWAYS);

        table = new TableView<>();
        table.setPlaceholder(new Label("No overdue books — great! 🎉"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colUser = new TableColumn<>("User");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUserName()));
        colUser.setMinWidth(140);

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Book");
        colBook.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookTitle()));
        colBook.setMinWidth(180);

        TableColumn<BorrowRecord, String> colBorrow = new TableColumn<>("Borrow Date");
        colBorrow.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBorrowDate().toString()));

        TableColumn<BorrowRecord, String> colDue = new TableColumn<>("Due Date");
        colDue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDueDate().toString()));

        TableColumn<BorrowRecord, String> colDays = new TableColumn<>("Days Overdue");
        colDays.setCellValueFactory(c -> new SimpleStringProperty(
            String.valueOf(c.getValue().getDaysOverdue())));
        colDays.setStyle("-fx-alignment: CENTER;");

        TableColumn<BorrowRecord, String> colFine = new TableColumn<>("Fine");
        colFine.setCellValueFactory(c -> new SimpleStringProperty(
            "$" + c.getValue().calculateFine().toString()));
        colFine.setStyle("-fx-alignment: CENTER;");

        TableColumn<BorrowRecord, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty("Overdue"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("badge", "badge-overdue");
                    setGraphic(badge);
                }
            }
        });

        table.getColumns().addAll(colUser, colBook, colBorrow, colDue, colDays, colFine, colStatus);

        // Refresh button
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadOverdue());

        HBox toolbar = new HBox(refreshBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        toolbar.getStyleClass().add("toolbar");

        tablePanel.getChildren().add(table);
        getChildren().addAll(title, subtitle, infoCard, toolbar, tablePanel);
    }

    private void loadOverdue() {
        List<BorrowRecord> records = borrowService.getOverdueRecords();
        table.setItems(FXCollections.observableArrayList(records));
    }
}
