package com.library.view;

import com.library.database.BorrowRecordDAO;
import com.library.database.UserDAO;
import com.library.model.BorrowRecord;
import com.library.model.User;
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

import java.util.List;

/**
 * User management view: list, search, add, edit, delete users.
 * Includes ability to view borrowing records per user.
 */
public class UserView extends VBox {

    private final UserDAO userDAO = new UserDAO();
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
    private TableView<User> table;
    private ObservableList<User> userList;
    private TextField searchField;

    public UserView() {
        setSpacing(20);
        setPadding(new Insets(8));
        build();
        loadUsers();
    }

    private void build() {
        Label title = new Label("Users");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage library members");
        subtitle.getStyleClass().add("page-subtitle");

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");

        searchField = new TextField();
        searchField.setPromptText("🔍  Search users...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, o, n) -> handleSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button addBtn = new Button("+ Add User");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showUserForm(null));

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
        table.setPlaceholder(new Label("No users found"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<User, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colName.setMinWidth(160);

        TableColumn<User, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colUser.setMinWidth(120);

        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setMinWidth(180);

        TableColumn<User, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));

        TableColumn<User, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().name()));
        colRole.setMinWidth(80);
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    badge.getStyleClass().add(item.equals("ADMIN") ? "badge-admin" : "badge-student");
                    setGraphic(badge);
                }
            }
        });

        table.getColumns().addAll(colName, colUser, colEmail, colPhone, colRole);

        // Actions column
        if (SessionManager.getInstance().isAdmin()) {
            TableColumn<User, Void> colActions = new TableColumn<>("Actions");
            colActions.setMinWidth(240);
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button editBtn   = new Button("Edit");
                private final Button deleteBtn = new Button("Delete");
                private final Button histBtn   = new Button("History");
                private final HBox box = new HBox(6, editBtn, deleteBtn, histBtn);

                {
                    editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
                    deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");
                    histBtn.getStyleClass().addAll("btn-primary", "btn-small");
                    box.setAlignment(Pos.CENTER);

                    editBtn.setOnAction(e -> {
                        User user = getTableView().getItems().get(getIndex());
                        showUserForm(user);
                    });
                    deleteBtn.setOnAction(e -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleDelete(user);
                    });
                    histBtn.setOnAction(e -> {
                        User user = getTableView().getItems().get(getIndex());
                        showBorrowHistory(user);
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

    private void loadUsers() {
        userList = FXCollections.observableArrayList(userDAO.findAll());
        table.setItems(userList);
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadUsers();
        } else {
            userList = FXCollections.observableArrayList(userDAO.search(keyword));
            table.setItems(userList);
        }
    }

    private void handleDelete(User user) {
        if (user.getId() == SessionManager.getInstance().getCurrentUser().getId()) {
            AlertHelper.showWarning("Cannot Delete", "You cannot delete your own account.");
            return;
        }
        if (AlertHelper.showConfirm("Delete User",
            "Are you sure you want to delete \"" + user.getFullName() + "\"?\nAll borrow records will also be deleted.")) {
            if (userDAO.delete(user.getId())) {
                loadUsers();
                AlertHelper.showSuccess("Deleted", "User deleted successfully.");
            } else {
                AlertHelper.showError("Error", "Failed to delete user.");
            }
        }
    }

    /** Show add/edit user dialog */
    private void showUserForm(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add User" : "Edit User");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(24));
        grid.setMinWidth(480);

        TextField fullNameField = new TextField(); fullNameField.setPromptText("Full name");
        TextField usernameField = new TextField(); usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password (min 6 chars)");
        TextField emailField    = new TextField(); emailField.setPromptText("Email");
        TextField phoneField    = new TextField(); phoneField.setPromptText("Phone");
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("STUDENT", "ADMIN"));
        roleBox.setValue("STUDENT");

        if (existing != null) {
            fullNameField.setText(existing.getFullName());
            usernameField.setText(existing.getUsername());
            passwordField.setText(existing.getPassword());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            roleBox.setValue(existing.getRole().name());
        }

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        int row = 0;
        grid.add(new Label("Full Name *"),  0, row); grid.add(fullNameField,  1, row++);
        grid.add(new Label("Username *"),   0, row); grid.add(usernameField,  1, row++);
        grid.add(new Label("Password *"),   0, row); grid.add(passwordField,  1, row++);
        grid.add(new Label("Email"),        0, row); grid.add(emailField,     1, row++);
        grid.add(new Label("Phone"),        0, row); grid.add(phoneField,     1, row++);
        grid.add(new Label("Role"),         0, row); grid.add(roleBox,        1, row++);
        grid.add(errorLabel, 0, row, 2, 1);

        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setMinWidth(90);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(cc1, cc2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveType) {
                String err;

                err = ValidationHelper.validateRequired(fullNameField.getText(), "Full Name");
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                err = ValidationHelper.validateUsername(usernameField.getText());
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                err = ValidationHelper.validatePassword(passwordField.getText());
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                err = ValidationHelper.validateEmail(emailField.getText());
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                err = ValidationHelper.validatePhone(phoneField.getText());
                if (err != null) { errorLabel.setText(err); errorLabel.setVisible(true); return null; }

                // Check duplicate username
                if (existing == null || !usernameField.getText().trim().equals(existing.getUsername())) {
                    if (userDAO.usernameExists(usernameField.getText().trim())) {
                        errorLabel.setText("Username already exists.");
                        errorLabel.setVisible(true);
                        return null;
                    }
                }

                User user = (existing != null) ? existing : new User();
                user.setFullName(fullNameField.getText().trim());
                user.setUsername(usernameField.getText().trim());
                user.setPassword(passwordField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setPhone(phoneField.getText().trim());
                user.setRole(User.Role.valueOf(roleBox.getValue()));
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            boolean success;
            if (existing != null) {
                success = userDAO.update(user);
            } else {
                success = userDAO.insert(user);
            }
            if (success) {
                loadUsers();
                AlertHelper.showSuccess("Success", "User saved successfully.");
            } else {
                AlertHelper.showError("Error", "Failed to save user.");
            }
        });
    }

    /** Show borrow history dialog for a user */
    private void showBorrowHistory(User user) {
        List<BorrowRecord> records = borrowRecordDAO.findByUserId(user.getId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Borrow History — " + user.getFullName());
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setMinWidth(700);
        dialog.getDialogPane().setMinHeight(400);

        TableView<BorrowRecord> histTable = new TableView<>();
        histTable.setItems(FXCollections.observableArrayList(records));
        histTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        histTable.setPlaceholder(new Label("No borrow records found for this user."));

        TableColumn<BorrowRecord, String> colBook = new TableColumn<>("Book");
        colBook.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookTitle()));
        colBook.setMinWidth(200);

        TableColumn<BorrowRecord, String> colBorrow = new TableColumn<>("Borrowed");
        colBorrow.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBorrowDate().toString()));

        TableColumn<BorrowRecord, String> colDue = new TableColumn<>("Due");
        colDue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDueDate().toString()));

        TableColumn<BorrowRecord, String> colReturn = new TableColumn<>("Returned");
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

        histTable.getColumns().addAll(colBook, colBorrow, colDue, colReturn, colStatus, colFine);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        Label headerLabel = new Label("Borrow records for " + user.getFullName());
        headerLabel.getStyleClass().add("panel-title");
        content.getChildren().addAll(headerLabel, histTable);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
}
