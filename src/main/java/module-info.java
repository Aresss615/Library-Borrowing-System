module com.library {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens com.library to javafx.fxml;
    opens com.library.controller to javafx.fxml;
    opens com.library.model to javafx.base;
    opens com.library.view to javafx.fxml;

    exports com.library;
    exports com.library.controller;
    exports com.library.model;
    exports com.library.view;
    exports com.library.service;
    exports com.library.database;
    exports com.library.utils;
}
