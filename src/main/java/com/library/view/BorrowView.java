package com.library.view;

import com.library.database.BookDAO;
import com.library.database.BorrowRecordDAO;
import com.library.database.UserDAO;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import com.library.service.BorrowService;
import com.library.utils.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Completely redesigned Borrowing view with three sections:
 *   1. Borrow Book  — live-search student + book → summary → confirm
 *   2. Return Book  — search active borrows → select → confirm return
 *   3. History      — searchable/filterable table of all records
 */
public class BorrowView extends VBox {

    private final BorrowService borrowService = new BorrowService();
    private final BookDAO bookDAO = new BookDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();

    // Tab buttons
    private ToggleButton borrowTab;
    private ToggleButton returnTab;
    private ToggleButton historyTab;
    private StackPane contentStack;

    // ── Borrow tab state ──
    private User selectedStudent;
    private Book selectedBook;
    private VBox borrowSummaryCard;
    private Label studentChip;
    private Label bookChip;
    private TableView<User> studentTable;
    private TableView<Book> bookTable;
    private TextField studentSearchField;
    private TextField bookSearchField;

    // ── Return tab state ──
    private BorrowRecord selectedRecord;
    private VBox returnSummaryCard;
    private TableView<BorrowRecord> activeTable;

    // ── History tab state ──
    private TableView<BorrowRecord> historyTable;
    private ComboBox<String> filterBox;
    private TextField historySearchField;

    public BorrowView() {
        setSpacing(20);
        setPadding(new Insets(8));
        buildHeader();
        buildContent();
        showBorrowTab();
    }

    // ══════════════════════════════════════════════════════════
    //  HEADER  – title + pill-toggle tab bar
    // ══════════════════════════════════════════════════════════

    private void buildHeader() {
        Label title = new Label("Borrowing");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage book loans and returns");
        subtitle.getStyleClass().add("page-subtitle");

        HBox tabBar = new HBox(0);
        tabBar.getStyleClass().add("pill-toggle-bar");
        tabBar.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup tabGroup = new ToggleGroup();

        borrowTab = new ToggleButton("Borrow Book");
        borrowTab.setToggleGroup(tabGroup);
        borrowTab.getStyleClass().add("pill-toggle");
        borrowTab.setOnAction(e -> showBorrowTab());

        returnTab = new ToggleButton("Return Book");
        returnTab.setToggleGroup(tabGroup);
        returnTab.getStyleClass().add("pill-toggle");
        returnTab.setOnAction(e -> showReturnTab());

        historyTab = new ToggleButton("History");
        historyTab.setToggleGroup(tabGroup);
        historyTab.getStyleClass().add("pill-toggle");
        historyTab.setOnAction(e -> showHistoryTab());

        // Prevent deselecting the active toggle
        tabGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) tabGroup.selectToggle(oldT);
        });

        tabBar.getChildren().addAll(borrowTab, returnTab, historyTab);
        getChildren().addAll(title, subtitle, tabBar);
    }

    private void buildContent() {
        contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);
        getChildren().add(contentStack);
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 1 — BORROW BOOK
    // ══════════════════════════════════════════════════════════

    private void showBorrowTab() {
        borrowTab.setSelected(true);
        selectedStudent = null;
        selectedBook = null;

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(4, 0, 24, 0));

        VBox studentSection = buildStudentSearchSection();
        VBox bookSection    = buildBookSearchSection();

        borrowSummaryCard = new VBox(16);
        borrowSummaryCard.getStyleClass().addAll("glass-panel", "summary-card");
        borrowSummaryCard.setVisible(false);
        borrowSummaryCard.setManaged(false);

        content.getChildren().addAll(studentSection, bookSection, borrowSummaryCard);
        scroll.setContent(content);
        contentStack.getChildren().setAll(scroll);
    }

    /* ── Student search panel ── */

    private VBox buildStudentSearchSection() {
        VBox section = new VBox(12);
        section.getStyleClass().add("glass-panel");

        Label sectionTitle = new Label("Step 1 — Select Student");
        sectionTitle.getStyleClass().add("section-title");

        studentSearchField = new TextField();
        studentSearchField.setPromptText("Search by name, username, ID, or email...");
        studentSearchField.getStyleClass().add("search-field");
        studentSearchField.textProperty().addListener((obs, o, n) -> searchStudents(n));

        studentChip = new Label();
        studentChip.getStyleClass().add("selection-chip");
        studentChip.setVisible(false);
        studentChip.setManaged(false);

        studentTable = new TableView<>();
        studentTable.setPlaceholder(new Label("Type to search for a student"));
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        studentTable.setPrefHeight(180);
        studentTable.setMaxHeight(180);

        TableColumn<User, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setMaxWidth(60);
        colId.setMinWidth(60);

        TableColumn<User, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colName.setMinWidth(160);

        TableColumn<User, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));

        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        studentTable.getColumns().addAll(colId, colName, colUsername, colEmail);

        studentTable.setOnMouseClicked(e -> {
            User sel = studentTable.getSelectionModel().getSelectedItem();
            if (sel != null) selectStudent(sel);
        });

        section.getChildren().addAll(sectionTitle, studentSearchField, studentChip, studentTable);
        return section;
    }

    /* ── Book search panel ── */

    private VBox buildBookSearchSection() {
        VBox section = new VBox(12);
        section.getStyleClass().add("glass-panel");

        Label sectionTitle = new Label("Step 2 — Select Book");
        sectionTitle.getStyleClass().add("section-title");

        bookSearchField = new TextField();
        bookSearchField.setPromptText("Search by title, author, ISBN, or category...");
        bookSearchField.getStyleClass().add("search-field");
        bookSearchField.textProperty().addListener((obs, o, n) -> searchBooks(n));

        bookChip = new Label();
        bookChip.getStyleClass().add("selection-chip");
        bookChip.setVisible(false);
        bookChip.setManaged(false);

        bookTable = new TableView<>();
        bookTable.setPlaceholder(new Label("Type to search for a book"));
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        bookTable.setPrefHeight(180);
        bookTable.setMaxHeight(180);

        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colTitle.setMinWidth(200);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Author");
        colAuthor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuthor()));

        TableColumn<Book, String> colISBN = new TableColumn<>("ISBN");
        colISBN.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIsbn()));

        TableColumn<Book, String> colAvail = new TableColumn<>("Available");
        colAvail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAvailableCopies() + " / " + c.getValue().getTotalCopies()));
        colAvail.setMinWidth(90);
        colAvail.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if (book.getAvailableCopies() == 0) {
                        badge.getStyleClass().add("badge-overdue");
                    } else if (book.getAvailableCopies() <= 1) {
                        badge.getStyleClass().add("badge-low-stock");
                    } else {
                        badge.getStyleClass().add("badge-returned");
                    }
                    setGraphic(badge);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        bookTable.getColumns().addAll(colTitle, colAuthor, colISBN, colAvail);

        bookTable.setOnMouseClicked(e -> {
            Book sel = bookTable.getSelectionModel().getSelectedItem();
            if (sel != null) selectBook(sel);
        });

        section.getChildren().addAll(sectionTitle, bookSearchField, bookChip, bookTable);
        return section;
    }

    /* ── Search helpers ── */

    private void searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            studentTable.setItems(FXCollections.observableArrayList());
            return;
        }
        studentTable.setItems(FXCollections.observableArrayList(userDAO.searchStudents(keyword.trim())));
    }

    private void searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            bookTable.setItems(FXCollections.observableArrayList());
            return;
        }
        bookTable.setItems(FXCollections.observableArrayList(bookDAO.searchAvailable(keyword.trim())));
    }

    /* ── Selection handlers ── */

    private void selectStudent(User student) {
        selectedStudent = student;
        int active = borrowRecordDAO.countActiveByUser(student.getId());
        studentChip.setText("\u2713  " + student.getFullName() + "  (" + student.getUsername() + ")  \u2014  " + active + " active borrow(s)");
        studentChip.setVisible(true);
        studentChip.setManaged(true);
        updateBorrowSummary();
    }

    private void selectBook(Book book) {
        if (book.getAvailableCopies() <= 0) {
            AlertHelper.showWarning("Unavailable", "\"" + book.getTitle() + "\" has no available copies right now.");
            return;
        }
        selectedBook = book;
        bookChip.setText("\u2713  " + book.getTitle() + "  \u2014  " + book.getAvailableCopies() + " available");
        bookChip.setVisible(true);
        bookChip.setManaged(true);
        updateBorrowSummary();
    }

    /* ── Summary card ── */

    private void updateBorrowSummary() {
        if (selectedStudent == null || selectedBook == null) {
            borrowSummaryCard.setVisible(false);
            borrowSummaryCard.setManaged(false);
            return;
        }

        borrowSummaryCard.getChildren().clear();

        Label heading = new Label("Borrow Summary");
        heading.getStyleClass().add("section-title");

        int activeCount = borrowRecordDAO.countActiveByUser(selectedStudent.getId());
        boolean duplicate = borrowRecordDAO.hasActiveBorrow(selectedStudent.getId(), selectedBook.getId());

        LocalDate today   = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(10);

        int row = 0;
        addSummaryRow(grid, row++, "Student",          selectedStudent.getFullName() + "  (" + selectedStudent.getUsername() + ")");
        addSummaryRow(grid, row++, "Active Borrows",   activeCount + " / 5");
        addSummaryRow(grid, row++, "Book",             selectedBook.getTitle() + "  by  " + selectedBook.getAuthor());
        addSummaryRow(grid, row++, "Available Copies", selectedBook.getAvailableCopies() + " / " + selectedBook.getTotalCopies());
        addSummaryRow(grid, row++, "Borrow Date",      today.format(fmt));
        addSummaryRow(grid, row++, "Due Date",         dueDate.format(fmt) + "  (14 days)");

        VBox warnings = new VBox(6);
        if (activeCount >= 5) {
            warnings.getChildren().add(warningLabel("Student has reached the maximum borrow limit (5 books)."));
        }
        if (duplicate) {
            warnings.getChildren().add(warningLabel("Student already has this book borrowed."));
        }
        if (selectedBook.getAvailableCopies() == 1) {
            warnings.getChildren().add(warningLabel("Last available copy!"));
        }

        Button confirmBtn = new Button("Confirm Borrow");
        confirmBtn.getStyleClass().addAll("btn-primary", "btn-large");
        confirmBtn.setOnAction(e -> handleBorrow());
        confirmBtn.setDisable(activeCount >= 5 || duplicate);

        Button clearBtn = new Button("Clear Selection");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> resetBorrowForm());

        HBox buttons = new HBox(12, confirmBtn, clearBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        borrowSummaryCard.getChildren().add(heading);
        borrowSummaryCard.getChildren().add(grid);
        if (!warnings.getChildren().isEmpty()) borrowSummaryCard.getChildren().add(warnings);
        borrowSummaryCard.getChildren().add(buttons);

        borrowSummaryCard.setVisible(true);
        borrowSummaryCard.setManaged(true);
    }

    private void handleBorrow() {
        if (selectedStudent == null || selectedBook == null) return;

        String error = borrowService.borrowBook(selectedStudent.getId(), selectedBook.getId());
        if (error == null) {
            AlertHelper.showSuccess("Success",
                    "\"" + selectedBook.getTitle() + "\" borrowed to " + selectedStudent.getFullName() + ".");
            resetBorrowForm();
        } else {
            AlertHelper.showError("Borrow Failed", error);
        }
    }

    private void resetBorrowForm() {
        selectedStudent = null;
        selectedBook = null;
        studentSearchField.clear();
        bookSearchField.clear();
        studentChip.setVisible(false);  studentChip.setManaged(false);
        bookChip.setVisible(false);     bookChip.setManaged(false);
        studentTable.setItems(FXCollections.observableArrayList());
        bookTable.setItems(FXCollections.observableArrayList());
        borrowSummaryCard.setVisible(false);
        borrowSummaryCard.setManaged(false);
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 2 — RETURN BOOK
    // ══════════════════════════════════════════════════════════

    private void showReturnTab() {
        returnTab.setSelected(true);
        selectedRecord = null;

        VBox content = new VBox(20);
        content.setPadding(new Insets(4, 0, 24, 0));

        // ── Search section ──
        VBox searchSection = new VBox(12);
        searchSection.getStyleClass().add("glass-panel");

        Label sectionTitle = new Label("Search Active Borrows");
        sectionTitle.getStyleClass().add("section-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by borrower name, username, book title, or borrow ID...");
        searchField.getStyleClass().add("search-field");

        activeTable = new TableView<>();
        activeTable.setPlaceholder(new Label("No active borrows found"));
        activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        activeTable.setPrefHeight(280);
        VBox.setVgrow(activeTable, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colId = new TableColumn<>("#");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setMaxWidth(50); colId.setMinWidth(50);

        TableColumn<BorrowRecord, String> colUser = new TableColumn<>("Borrower");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUserName()));
        colUser.setMinWidth(140);

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Book");
        colBook.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookTitle()));
        colBook.setMinWidth(180);

        TableColumn<BorrowRecord, String> colBorrow = new TableColumn<>("Borrowed");
        colBorrow.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBorrowDate().toString()));

        TableColumn<BorrowRecord, String> colDue = new TableColumn<>("Due Date");
        colDue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDueDate().toString()));

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
                    if ("Overdue".equals(item)) badge.getStyleClass().add("badge-overdue");
                    else badge.getStyleClass().add("badge-borrowed");
                    setGraphic(badge);
                }
            }
        });

        activeTable.getColumns().addAll(colId, colUser, colBook, colBorrow, colDue, colStatus);

        // Load all active borrows by default, filter on typing
        activeTable.setItems(FXCollections.observableArrayList(borrowService.getBorrowedRecords()));

        searchField.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.trim().isEmpty()) {
                activeTable.setItems(FXCollections.observableArrayList(borrowService.getBorrowedRecords()));
            } else {
                activeTable.setItems(FXCollections.observableArrayList(borrowRecordDAO.searchActive(n.trim())));
            }
            // Clear return selection when search changes
            selectedRecord = null;
            returnSummaryCard.setVisible(false);
            returnSummaryCard.setManaged(false);
        });

        Label hint = new Label("Click a row to select it for return");
        hint.getStyleClass().add("muted-text");

        searchSection.getChildren().addAll(sectionTitle, searchField, activeTable, hint);

        // ── Return summary card ──
        returnSummaryCard = new VBox(16);
        returnSummaryCard.getStyleClass().addAll("glass-panel", "summary-card");
        returnSummaryCard.setVisible(false);
        returnSummaryCard.setManaged(false);

        activeTable.setOnMouseClicked(e -> {
            BorrowRecord sel = activeTable.getSelectionModel().getSelectedItem();
            if (sel != null) selectForReturn(sel);
        });

        content.getChildren().addAll(searchSection, returnSummaryCard);
        contentStack.getChildren().setAll(content);
    }

    private void selectForReturn(BorrowRecord record) {
        selectedRecord = record;
        returnSummaryCard.getChildren().clear();

        Label heading = new Label("Return Summary");
        heading.getStyleClass().add("section-title");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(10);
        int row = 0;
        addSummaryRow(grid, row++, "Borrower",    record.getUserName());
        addSummaryRow(grid, row++, "Book",         record.getBookTitle());
        addSummaryRow(grid, row++, "Borrowed",     record.getBorrowDate().format(fmt));
        addSummaryRow(grid, row++, "Due Date",     record.getDueDate().format(fmt));

        VBox extras = new VBox(6);
        if (record.isOverdue()) {
            addSummaryRow(grid, row++, "Days Overdue", record.getDaysOverdue() + " days");
            addSummaryRow(grid, row++, "Fine",         "$" + record.calculateFine().toPlainString());
            extras.getChildren().add(
                    warningLabel("This book is overdue. A fine of $" + record.calculateFine().toPlainString() + " will be applied."));
        } else {
            addSummaryRow(grid, row++, "Status", "On time");
            addSummaryRow(grid, row++, "Fine",   "$0.00");
        }

        Button confirmBtn = new Button("Confirm Return");
        confirmBtn.getStyleClass().addAll("btn-success", "btn-large");
        confirmBtn.setOnAction(e -> handleReturn());

        HBox buttons = new HBox(12, confirmBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        returnSummaryCard.getChildren().add(heading);
        returnSummaryCard.getChildren().add(grid);
        if (!extras.getChildren().isEmpty()) returnSummaryCard.getChildren().add(extras);
        returnSummaryCard.getChildren().add(buttons);
        returnSummaryCard.setVisible(true);
        returnSummaryCard.setManaged(true);
    }

    private void handleReturn() {
        if (selectedRecord == null) return;

        String error = borrowService.returnBook(selectedRecord);
        if (error == null) {
            AlertHelper.showSuccess("Returned",
                    "\"" + selectedRecord.getBookTitle() + "\" returned successfully.");
            showReturnTab(); // refresh
        } else {
            AlertHelper.showError("Return Failed", error);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 3 — HISTORY
    // ══════════════════════════════════════════════════════════

    private void showHistoryTab() {
        historyTab.setSelected(true);

        VBox content = new VBox(16);
        content.setPadding(new Insets(4, 0, 24, 0));

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        historySearchField = new TextField();
        historySearchField.setPromptText("Search records...");
        historySearchField.getStyleClass().add("search-field");
        HBox.setHgrow(historySearchField, Priority.ALWAYS);

        filterBox = new ComboBox<>(FXCollections.observableArrayList("All", "Borrowed", "Returned", "Overdue"));
        filterBox.setValue("All");
        filterBox.setOnAction(e -> loadHistoryRecords());

        historySearchField.textProperty().addListener((obs, o, n) -> loadHistoryRecords());

        toolbar.getChildren().addAll(historySearchField, filterBox);

        // Table
        VBox tablePanel = new VBox();
        tablePanel.getStyleClass().add("glass-panel");
        VBox.setVgrow(tablePanel, Priority.ALWAYS);

        historyTable = new TableView<>();
        historyTable.setPlaceholder(new Label("No borrow records found"));
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        TableColumn<BorrowRecord, String> colId = new TableColumn<>("#");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setMaxWidth(50);

        TableColumn<BorrowRecord, String> colUser = new TableColumn<>("Borrower");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUserName()));
        colUser.setMinWidth(140);

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Book");
        colBook.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookTitle()));
        colBook.setMinWidth(180);

        TableColumn<BorrowRecord, String> colBorrow = new TableColumn<>("Borrowed");
        colBorrow.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBorrowDate().toString()));

        TableColumn<BorrowRecord, String> colDue = new TableColumn<>("Due Date");
        colDue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDueDate().toString()));

        TableColumn<BorrowRecord, String> colReturn = new TableColumn<>("Returned");
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

        historyTable.getColumns().addAll(colId, colUser, colBook, colBorrow, colDue, colReturn, colStatus, colFine);

        tablePanel.getChildren().add(historyTable);
        content.getChildren().addAll(toolbar, tablePanel);
        contentStack.getChildren().setAll(content);

        loadHistoryRecords();
    }

    private void loadHistoryRecords() {
        String keyword = historySearchField != null ? historySearchField.getText().trim() : "";
        String filter  = filterBox != null ? filterBox.getValue() : "All";

        List<BorrowRecord> records;
        if (!keyword.isEmpty()) {
            records = borrowService.searchRecords(keyword);
        } else {
            records = switch (filter) {
                case "Borrowed" -> borrowService.getBorrowedRecords();
                case "Returned" -> borrowService.getReturnedRecords();
                case "Overdue"  -> borrowService.getOverdueRecords();
                default         -> borrowService.getAllRecords();
            };
        }
        historyTable.setItems(FXCollections.observableArrayList(records));
    }

    // ══════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════════════════════

    private void addSummaryRow(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("summary-label");
        Label v = new Label(value);
        v.getStyleClass().add("summary-value");
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private Label warningLabel(String text) {
        Label lbl = new Label("\u26A0  " + text);
        lbl.getStyleClass().add("warning-text");
        return lbl;
    }
}
