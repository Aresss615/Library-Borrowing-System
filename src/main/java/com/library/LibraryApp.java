package com.library;

import com.library.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the Library Borrowing System.
 * Launches the JavaFX application and shows the Login screen.
 */
public class LibraryApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        // Create scene and apply stylesheet
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        // Configure stage
        primaryStage.setTitle("Library — Sign In");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(550);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Clean up database connection on exit
        DatabaseConnection.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
