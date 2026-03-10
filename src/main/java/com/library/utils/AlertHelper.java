package com.library.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * Utility class for showing styled dialog/alert boxes.
 * Applies Apple-inspired styling to all dialogs.
 */
public class AlertHelper {

    /** Show an info alert */
    public static void showInfo(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION, title, message);
        alert.showAndWait();
    }

    /** Show a success alert */
    public static void showSuccess(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION, title, message);
        alert.setHeaderText("Success");
        alert.showAndWait();
    }

    /** Show a warning alert */
    public static void showWarning(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.WARNING, title, message);
        alert.showAndWait();
    }

    /** Show an error alert */
    public static void showError(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.ERROR, title, message);
        alert.showAndWait();
    }

    /** Show a confirmation dialog, returns true if user clicks OK */
    public static boolean showConfirm(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /** Create a styled alert */
    private static Alert createAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply styling to the dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #FAFAFA;" +
            "-fx-font-family: 'Segoe UI', 'SF Pro Display', system;" +
            "-fx-font-size: 13px;"
        );
        return alert;
    }
}
