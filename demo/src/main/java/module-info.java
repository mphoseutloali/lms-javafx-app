module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Export packages to JavaFX FXML
    exports com.example.demo;
    exports com.example.demo.lms.controller;
    exports com.example.demo.lms.model;
    exports com.example.demo.lms.util;

    // Open packages to JavaFX FXML for reflection
    opens com.example.demo to javafx.fxml;
    opens com.example.demo.lms.controller to javafx.fxml;
    opens com.example.demo.lms.model to javafx.fxml;

}
