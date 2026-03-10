package com.library.view;

import com.library.database.BookDAO;
import com.library.database.UserDAO;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import com.library.service.BorrowService;
import com.library.utils.AlertHelper;
import com.library.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Borrowing view: borrow/return books, view active borrows and history.
 */
public class BorrowView extends VBox {

    private final BorrowService borrowService = new BorrowService();
    private final BookDAO bookDAO = new BookDAO();
    private final UserDAO userDAO = new UserDAO();
    private TableView<BorrowRecord> table;
    private ObservableList<BorrowRecord> recordList;
    private TextField searchField;
    private ComboBox<String> filterBox;

    public BorrowView() {
        setSpacing(20);
        setPadding(new Insets(8));
        build();
        loadRecords("All");
    }

    private void build() {
        Label title = new Label("Borrowing");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage book loans and returns");
        subtitle.getStyleClass().add("page-subtitle");

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        searchField = new TextField();
        searchField.setPromptText("🔍  Search by user or book...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, o, n) -> handleSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterBox = new ComboBox<>(FXCollections.observableArrayList("All", "Borrowed", "Returned", "Overdue"));
        filterBox.setValue("All");
        filterBox.setOnAction(e -> loadRecords(filterBox.getValue()));

        Button borrowBtn = new Button("+ Borrow Book");
        borrowBtn.getStyleClass().add("btn-primary");
        borrowBtn.setOnAction(e -> showBorrowDialog());

        if (!SessionManager.getInstance().isAdmin()) {
            borrowBtn.setVisible(false);
            borrowBtn.setManaged(false);
        }

        toolbar.getChildren().addAll(searchField, filterBox, borrowBtn);

        // Table
        VBox tablePanel = new VBox();
        tablePanel.getStyleClass().add("glass-panel");
        VBox.setVgrow(tablePanel, Priority.ALWAYS);

        table = new TableView<>();
        table.setPlaceholder(new Label("No borrow records found"));
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
        colFine.setMinWidth(60);

        table.getColumns().addAll(colUser, colBook, colBorrow, colDue, colReturn, colStatus, colFine);

        // Return action column (admin only)
        if (SessionManager.getInstance().isAdmin()) {
            TableColumn<BorrowRecord, Void> colActions = new TableColumn<>("Action");
            colActions.setMinWidth(100);
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button returnBtn = new Button("Return");

                {
                    returnBtn.getStyleClass().addAll("btn-success", "btn-small");
                    returnBtn.setOnAction(e -> {
                        BorrowRecord record = getTableView().getItems().get(getIndex());
                        handleReturn(record);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        BorrowRecord record = getTableView().getItems().get(getIndex());
                        if (record.getStatus() != BorrowRecord.Status.RETURNED) {
                            setGraphic(returnBtn);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
            table.getColumns().add(colActions);
        }

        tablePanel.getChildren().add(table);
        getChildren().addAll(title, subtitle, toolbar, tablePanel);
    }

    private void loadRecords(String filter) {
        List<BorrowRecord> records;
        switch (filter) {
            case "Borrowed" -> records = borrowService.getBorrowedRecords();
            case "Returned" -> records = borrowService.getReturnedRecords();
            case "Overdue"  -> records = borrowService.getOverdueRecords();
            default         -> records = borrowService.getAllRecords();
        }
        recordList = FXCollections.observableArrayList(records);
        table.setItems(recordList);
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadRecords(filterBox.getValue());
        } else {
            recordList = FXCollections.observableArrayList(borrowService.searchRecords(keyword));
            table.setItems(recordList);
        }
    }

    private void handleReturn(BorrowRecord record) {
        String fineInfo = "";
        if (record.isOverdue()) {
            fineInfo = "\nThis book is " + record.getDaysOverdue() + " days overdue. Fine: $" + record.calculateFine();
        }
        if (AlertHelper.showConfirm("Return Book",
            "Return \"" + record.getBookTitle() + "\"?" + fineInfo)) {
            String error = borrowService.returnBook(record);
            if (error == null) {
                loadRecords(filterBox.getValue());
                AlertHelper.showSuccess("Returned", "Book returned successfully.");
            } else {
                AlertHelper.showError("Error", error);
            }
        }
    }

    /** Show dialog to create a new borrow record */
    private void showBorrowDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Borrow Book");
        dialog.setHeaderText(null);

        ButtonType borrowType = new ButtonType("Borrow", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(borrowType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(24));
        grid.setMinWidth(500);

        // User selection
        List<User> students = userDAO.findAllStudents();
        ComboBox<User> userCombo = new ComboBox<>(FXCollections.observableArrayList(students));
        userCombo.setPromptText("Select a student...");
        userCombo.setMaxWidth(Double.MAX_VALUE);

        // Book selection (only available books)
        List<Book> availableBooks = bookDAO.findAll().stream()
            .filter(b -> b.getAvailableCopies() > 0)
            .toList();
        ComboBox<Book> bookCombo = new ComboBox<>(FXCollections.observableArrayList(availableBooks));
        bookCombo.setPromptText("Select a book...");
        bookCombo.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        Label infoLabel = new Label("The book will be borrowed for 14 days.");
        infoLabel.getStyleClass().add("muted-text");

        grid.add(new Label("Student *"), 0, 0); grid.add(userCombo, 1, 0);
        grid.add(new Label("Book *"),    0, 1); grid.add(bookCombo, 1, 1);
        grid.add(infoLabel, 0, 2, 2, 1);
        grid.add(errorLabel, 0, 3, 2, 1);

        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setMinWidth(80);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(cc1, cc2);

        dialog.getDialogPane().setContent(grid);

        // Handle borrow button
        Button borrowButton = (Button) dialog.getDialogPane().lookupButton(borrowType);
        borrowButton.setOnAction(event -> {
            if (userCombo.getValue() == null) {
                errorLabel.setText("Please select a student.");
                errorLabel.setVisible(true);
                return;
            }
            if (bookCombo.getValue() == null) {
                errorLabel.setText("Please select a book.");
                errorLabel.setVisible(true);
                return;
            }

            String error = borrowService.borrowBook(
                userCombo.getValue().getId(),
                bookCombo.getValue().getId()
            );

            if (error == null) {
                loadRecords(filterBox.getValue());
                AlertHelper.showSuccess("Success", "Book borrowed successfully!");
                dialog.close();
            } else {
                errorLabel.setText(error);
                errorLabel.setVisible(true);
            }
        });

        dialog.showAndWait();
    }
}
