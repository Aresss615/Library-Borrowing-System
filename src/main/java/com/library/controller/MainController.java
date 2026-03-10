package com.library.controller;

import com.library.model.User;
import com.library.utils.SessionManager;
import com.library.view.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controller for the main layout with sidebar navigation.
 * Manages page switching and sidebar state.
 */
public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label userInfoLabel;

    // Navigation buttons
    @FXML private Button navDashboard;
    @FXML private Button navBooks;
    @FXML private Button navUsers;
    @FXML private Button navBorrow;
    @FXML private Button navOverdue;
    @FXML private Button navReports;

    private Button activeButton;

    @FXML
    public void initialize() {
        // Show user info in sidebar
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userInfoLabel.setText(user.getFullName() + " • " + user.getRole().name());
        }

        // Show dashboard by default
        showDashboard();
    }

    // ── Navigation Methods ──────────────────────────────────

    @FXML
    private void showDashboard() {
        setActiveNav(navDashboard);
        contentArea.getChildren().setAll(new DashboardView());
    }

    @FXML
    private void showBooks() {
        setActiveNav(navBooks);
        contentArea.getChildren().setAll(new BookView());
    }

    @FXML
    private void showUsers() {
        setActiveNav(navUsers);
        contentArea.getChildren().setAll(new UserView());
    }

    @FXML
    private void showBorrowing() {
        setActiveNav(navBorrow);
        contentArea.getChildren().setAll(new BorrowView());
    }

    @FXML
    private void showOverdue() {
        setActiveNav(navOverdue);
        contentArea.getChildren().setAll(new OverdueView());
    }

    @FXML
    private void showReports() {
        setActiveNav(navReports);
        contentArea.getChildren().setAll(new ReportsView());
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Library — Sign In");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Update the active navigation button styling */
    private void setActiveNav(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        activeButton = button;
    }
}
