package com.example.demo.lms.controller;

import com.example.demo.lms.dao.AdminDAO;
import com.example.demo.lms.model.Admin;
import com.example.demo.lms.util.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private AdminDAO adminDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        adminDAO = new AdminDAO();

        // Set focus to username field
        Platform.runLater(() -> usernameField.requestFocus());

        // Add enter key handler for password field
        passwordField.setOnAction(this::handleLogin);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required");
            return;
        }

        // Validate login credentials
        boolean isValid = adminDAO.validateLogin(username, password);

        if (isValid) {
            // Update last login time
            adminDAO.updateLastLogin(username);

            // Get admin details and store in session
            Admin admin = adminDAO.getAdminByUsername(username);
            SessionManager.login(admin);


            loadMainApplication();
        } else {
            showError("Invalid username or password");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void loadMainApplication() {
        try {
            // Load the main view
            URL mainViewUrl = getClass().getResource("/com/example/demo/fxml/MainView.fxml");
            if (mainViewUrl == null) {
                throw new IOException("Cannot find MainView.fxml");
            }

            Parent root = FXMLLoader.load(mainViewUrl);

            // Get current stage
            Stage stage = (Stage) usernameField.getScene().getWindow();

            Scene scene = new Scene(root, 1024, 768);
            stage.setScene(scene);
            stage.setTitle("LMS - Learning Management System");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading application: " + e.getMessage());
        }
    }
}
