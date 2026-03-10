package com.library.view;

import com.library.database.BookDAO;
import com.library.model.Book;
import com.library.utils.AlertHelper;
import com.library.utils.SessionManager;
import com.library.utils.ValidationHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Book management view: list, search, add, edit, delete books.
 */
public class BookView extends VBox {

    private final BookDAO bookDAO = new BookDAO();
    private TableView<Book> table;
    private ObservableList<Book> bookList;
    private TextField searchField;

    public BookView() {
        setSpacing(20);
        setPadding(new Insets(8));
        build();
        loadBooks();
    }

    private void build() {
        // Header
        Label title = new Label("Books");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage your library catalog");
        subtitle.getStyleClass().add("page-subtitle");

        // Toolbar: search + add button
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        searchField = new TextField();
        searchField.setPromptText("🔍  Search by title, author, ISBN, category...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, o, n) -> handleSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Book");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showBookForm(null));

        // Only admins can add
        if (!SessionManager.getInstance().isAdmin()) {
            addBtn.setVisible(false);
            addBtn.setManaged(false);
        }

        toolbar.getChildren().addAll(searchField, addBtn);

        // Table
        VBox tablePanel = new VBox();
        tablePanel.getStyleClass().add("glass-panel");
        VBox.setVgrow(tablePanel, Priority.ALWAYS);

        table = new TableView<>();
        table.setPlaceholder(new Label("No books found"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Columns
        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colTitle.setMinWidth(200);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Author");
        colAuthor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuthor()));
        colAuthor.setMinWidth(150);

        TableColumn<Book, String> colISBN = new TableColumn<>("ISBN");
        colISBN.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIsbn()));
        colISBN.setMinWidth(140);

        TableColumn<Book, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));

        TableColumn<Book, String> colYear = new TableColumn<>("Year");
        colYear.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getYearPublished() > 0 ? String.valueOf(c.getValue().getYearPublished()) : ""));
        colYear.setMinWidth(60);

        TableColumn<Book, String> colAvail = new TableColumn<>("Available");
        colAvail.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getAvailableCopies() + " / " + c.getValue().getTotalCopies()));
        colAvail.setMinWidth(90);
        colAvail.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
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

        table.getColumns().addAll(colTitle, colAuthor, colISBN, colCategory, colYear, colAvail);

        // Admin-only actions column
        if (SessionManager.getInstance().isAdmin()) {
            TableColumn<Book, Void> colActions = new TableColumn<>("Actions");
            colActions.setMinWidth(160);
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Delete");
                private final HBox box = new HBox(8, editBtn, deleteBtn);

                {
                    editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
                    deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");
                    box.setAlignment(Pos.CENTER);

                    editBtn.setOnAction(e -> {
                        Book book = getTableView().getItems().get(getIndex());
                        showBookForm(book);
                    });
                    deleteBtn.setOnAction(e -> {
                        Book book = getTableView().getItems().get(getIndex());
                        handleDelete(book);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
            table.getColumns().add(colActions);
        }

        tablePanel.getChildren().add(table);
        getChildren().addAll(title, subtitle, toolbar, tablePanel);
    }

    /** Load all books from database */
    private void loadBooks() {
        bookList = FXCollections.observableArrayList(bookDAO.findAll());
        table.setItems(bookList);
    }

    /** Handle search input */
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadBooks();
        } else {
            bookList = FXCollections.observableArrayList(bookDAO.search(keyword));
            table.setItems(bookList);
        }
    }

    /** Delete a book with confirmation */
    private void handleDelete(Book book) {
        if (AlertHelper.showConfirm("Delete Book",
            "Are you sure you want to delete \"" + book.getTitle() + "\"?")) {
            if (bookDAO.delete(book.getId())) {
                loadBooks();
                AlertHelper.showSuccess("Deleted", "Book deleted successfully.");
            } else {
                AlertHelper.showError("Error", "Failed to delete book. It may have active borrows.");
            }
        }
    }

    /** Show add/edit book dialog */
    private void showBookForm(Book existing) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Book" : "Edit Book");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        // Form fields
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(24));
        grid.setMinWidth(500);

        TextField titleField     = new TextField(); titleField.setPromptText("Book title");
        TextField authorField    = new TextField(); authorField.setPromptText("Author name");
        TextField isbnField      = new TextField(); isbnField.setPromptText("ISBN");
        TextField publisherField = new TextField(); publisherField.setPromptText("Publisher");
        TextField yearField      = new TextField(); yearField.setPromptText("Year");
        TextField categoryField  = new TextField(); categoryField.setPromptText("Category");
        TextField copiesField    = new TextField(); copiesField.setPromptText("Total copies");

        // Pre-fill for edit
        if (existing != null) {
            titleField.setText(existing.getTitle());
            authorField.setText(existing.getAuthor());
            isbnField.setText(existing.getIsbn());
            publisherField.setText(existing.getPublisher());
            yearField.setText(existing.getYearPublished() > 0 ? String.valueOf(existing.getYearPublished()) : "");
            categoryField.setText(existing.getCategory());
            copiesField.setText(String.valueOf(existing.getTotalCopies()));
        }

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        int row = 0;
        grid.add(new Label("Title *"),     0, row); grid.add(titleField,     1, row++);
        grid.add(new Label("Author *"),    0, row); grid.add(authorField,    1, row++);
        grid.add(new Label("ISBN"),        0, row); grid.add(isbnField,      1, row++);
        grid.add(new Label("Publisher"),   0, row); grid.add(publisherField, 1, row++);
        grid.add(new Label("Year"),        0, row); grid.add(yearField,      1, row++);
        grid.add(new Label("Category"),    0, row); grid.add(categoryField,  1, row++);
        grid.add(new Label("Copies *"),    0, row); grid.add(copiesField,    1, row++);
        grid.add(errorLabel, 0, row, 2, 1);

        // Make text fields stretch
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setMinWidth(80);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(cc1, cc2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveType) {
                // Validate
                String err;

                err = ValidationHelper.validateRequired(titleField.getText(), "Title");
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                err = ValidationHelper.validateRequired(authorField.getText(), "Author");
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                err = ValidationHelper.validatePositiveInt(copiesField.getText(), "Copies");
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                err = ValidationHelper.validateYear(yearField.getText());
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                Book book = (existing != null) ? existing : new Book();
                book.setTitle(titleField.getText().trim());
                book.setAuthor(authorField.getText().trim());
                book.setIsbn(isbnField.getText().trim());
                book.setPublisher(publisherField.getText().trim());
                book.setCategory(categoryField.getText().trim());

                String yearText = yearField.getText().trim();
                book.setYearPublished(yearText.isEmpty() ? 0 : Integer.parseInt(yearText));

                int newTotal = Integer.parseInt(copiesField.getText().trim());
                if (existing != null) {
                    // Adjust available copies proportionally
                    int diff = newTotal - existing.getTotalCopies();
                    book.setAvailableCopies(Math.max(0, existing.getAvailableCopies() + diff));
                } else {
                    book.setAvailableCopies(newTotal);
                }
                book.setTotalCopies(newTotal);

                return book;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(book -> {
            boolean success;
            if (existing != null) {
                success = bookDAO.update(book);
            } else {
                success = bookDAO.insert(book);
            }
            if (success) {
                loadBooks();
                AlertHelper.showSuccess("Success", "Book saved successfully.");
            } else {
                AlertHelper.showError("Error", "Failed to save book.");
            }
        });
    }
}
